package com.onlymaker.scorpio.task;

import com.amazonaws.mws.model.GetReportRequest;
import com.amazonaws.mws.model.GetReportRequestListResponse;
import com.amazonaws.mws.model.ReportRequestInfo;
import com.amazonaws.mws.model.RequestReportResponse;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import com.amazonservices.mws.orders._2013_09_01.model.*;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;
import com.onlymaker.scorpio.data.*;
import com.onlymaker.scorpio.mws.*;
import org.apache.commons.lang3.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AmazonFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonFetcher.class);
    private static final long SECOND_IN_MS = 1000;
    @Value("${fetcher.order.retrospect.days}")
    Long orderRetrospectDays;
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;
    @Autowired
    HtmlPageService htmlPageService;
    @Autowired
    AmazonEntryRepository amazonEntryRepository;
    @Autowired
    AmazonEntrySnapshotRepository amazonEntrySnapshotRepository;
    @Autowired
    AmazonOrderRepository amazonOrderRepository;
    @Autowired
    AmazonOrderItemRepository amazonOrderItemRepository;
    @Autowired
    AmazonInventoryRepository amazonInventoryRepository;
    @Autowired
    AmazonReportLogRepository amazonReportLogRepository;
    @Autowired
    AmazonSellerSkuRepository amazonSellerSkuRepository;
    @Autowired
    AmazonInboundRepository amazonInboundRepository;
    @Autowired
    AmazonInboundItemRepository amazonInboundItemRepository;

    @Scheduled(cron = "${fetcher.cron}")
    public void everyday() {
        LOGGER.info("run fetcher ...");
        requestInventoryReport();
        fetchHtml();
        fetchInventoryReport();
        fetchOrder();
        fetchInbound();
    }

    private void fetchHtml() {
        String identity = "";
        Iterable<AmazonEntry> iterable = amazonEntryRepository.findAllByStatusOrderByMarketAscAsin(AmazonEntry.STATUS_ENABLED);
        for (AmazonEntry entry : iterable) {
            try {
                if (!Objects.equals(identity, entry.getMarket() + entry.getAsin())) {
                    amazonEntrySnapshotRepository.save(htmlPageService.parse(entry));
                }
                identity = entry.getMarket() + entry.getAsin();
                Thread.sleep(SECOND_IN_MS);
            } catch (Throwable t) {
                LOGGER.info("{} fetch html unexpected error: {}", entry.getMarket(), t, t.getMessage(), t);
            }
        }
    }

    private void fetchOrder() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            OrderService orderService = new OrderService(appInfo, mws);
            try {
                fetchOrderByCreateTime(orderService);
            } catch (Throwable t) {
                LOGGER.info("{} fetch order unexpected error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
            try {
                fetchOrderByUpdateTime(orderService);
            } catch (Throwable t) {
                LOGGER.info("{} fetch order unexpected error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
        }
    }

    private void fetchInbound() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            InboundService inboundService = new InboundService(appInfo, mws);
            ListInboundShipmentsResult result = inboundService
                    .getListInboundShipmentsResponseUpdatedLastDay()
                    .getListInboundShipmentsResult();
            result.getShipmentData().getMember().forEach(member -> processInboundShipmentInfo(member, mws.getMarketplace(), inboundService));
            String token = result.getNextToken();
            while (StringUtils.isNotEmpty(token)) {
                ListInboundShipmentsByNextTokenResult nextResult = inboundService
                        .getListInboundShipmentsResponseByNextToken(token)
                        .getListInboundShipmentsByNextTokenResult();
                nextResult.getShipmentData().getMember().forEach(member -> processInboundShipmentInfo(member, mws.getMarketplace(), inboundService));
                token = nextResult.getNextToken();
            }
        }
    }

    private void requestInventoryReport() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            ReportService reportService = new ReportService(appInfo, mws);
            try {
                RequestReportResponse requestReportResponse = reportService.requestReport(ReportService.REPORT_TYPE.get("inventory"));
                ReportRequestInfo requestInfo = requestReportResponse.getRequestReportResult().getReportRequestInfo();
                LOGGER.info("Request inventory report, id: {}", requestInfo.getReportRequestId());
                AmazonReportLog amazonReportLog = new AmazonReportLog();
                amazonReportLog.setRequestId(requestInfo.getReportRequestId());
                String generatedReportId = requestInfo.getGeneratedReportId();
                amazonReportLog.setReportId(generatedReportId == null ? "" : generatedReportId);
                amazonReportLog.setReportType(ReportService.REPORT_TYPE.get("inventory"));
                amazonReportLog.setStatus(0);
                amazonReportLog.setCreateTime(new Timestamp(requestInfo.getSubmittedDate().toGregorianCalendar().getTimeInMillis()));
                amazonReportLogRepository.save(amazonReportLog);
            } catch (Throwable t) {
                LOGGER.info("{} request inventory report error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
        }
    }

    private void fetchInventoryReport() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            ReportService reportService = new ReportService(appInfo, mws);
            try {
                GetReportRequestListResponse reportRequestListResponse = reportService.getReportRequestListCompleted(ReportService.REPORT_TYPE.get("inventory"));
                List<ReportRequestInfo> reportRequestInfos = reportRequestListResponse.getGetReportRequestListResult().getReportRequestInfoList();
                for (ReportRequestInfo reportRequestInfo : reportRequestInfos) {
                    AmazonReportLog log = amazonReportLogRepository.findOneByRequestIdAndStatus(reportRequestInfo.getReportRequestId(), 0);
                    if (log != null) {
                        String reportId = reportRequestInfo.getGeneratedReportId();
                        if (reportRequestInfo.getReportProcessingStatus().equals("_DONE_")) {
                            File report = new File("/tmp/report");
                            GetReportRequest request = reportService.prepareGetReport(reportId, new FileOutputStream(report));
                            reportService.getReport(request);
                            request.getReportOutputStream().close();
                            List<String> lines;
                            Map<String, Integer> fields = new HashMap<>();
                            String encoding = UniversalDetector.detectCharset(report);
                            if (encoding != null) {
                                LOGGER.debug("{} inventory report detect encoding: {}", mws.getMarketplace(), encoding);
                                lines = Files.readAllLines(report.toPath(), Charset.forName(encoding));
                            } else {
                                LOGGER.debug("{} inventory report default encoding: {}", mws.getMarketplace(), Charset.defaultCharset());
                                lines = Files.readAllLines(report.toPath());
                            }
                            for (String line : lines) {
                                LOGGER.debug("report line: {}", line);
                                String[] elements = line.split("\t");
                                //sku	fnsku	asin	product-name	condition	your-price	mfn-listing-exists	mfn-fulfillable-quantity	afn-listing-exists	afn-warehouse-quantity	afn-fulfillable-quantity	afn-unsellable-quantity	afn-reserved-quantity	afn-total-quantity	per-unit-volume	afn-inbound-working-quantity	afn-inbound-shipped-quantity	afn-inbound-receiving-quantity
                                if (line.startsWith("sku")) {
                                    for (int i = 0; i < elements.length; i++) {
                                        fields.put(elements[i], i);
                                    }
                                } else {
                                    String market = mws.getMarketplace();
                                    String fnSku = elements[fields.get("fnsku")];
                                    String sellerSku = elements[fields.get("sku")];
                                    Map<String, String> map = Utils.parseSellerSku(sellerSku);
                                    Date date = new Date(log.getCreateTime().getTime());
                                    AmazonInventory amazonInventory = amazonInventoryRepository.findByMarketAndFnSkuAndCreateDate(market, fnSku, date);
                                    if (amazonInventory == null) {
                                        amazonInventory = new AmazonInventory();
                                        amazonInventory.setMarket(market);
                                        amazonInventory.setFnSku(fnSku);
                                        amazonInventory.setCreateDate(date);
                                        amazonInventory.setAsin(elements[fields.get("asin")]);
                                        amazonInventory.setSellerSku(sellerSku);
                                        amazonInventory.setSku(map.get("sku"));
                                        amazonInventory.setSize(map.get("size"));
                                        amazonInventory.setInStockQuantity(Integer.parseInt(elements[fields.get("afn-fulfillable-quantity")]));
                                        amazonInventory.setTotalQuantity(Integer.parseInt(elements[fields.get("afn-total-quantity")]));
                                        amazonInventoryRepository.save(amazonInventory);
                                        LOGGER.info("{} saving inventory: {}", market, amazonInventory.getSellerSku());
                                    }
                                }
                            }
                        }
                        log.setReportId(reportId);
                        log.setStatus(1);
                        amazonReportLogRepository.save(log);
                    }
                }
            } catch (Throwable t) {
                LOGGER.info("{} fetch inventory report error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
        }
    }

    private void fetchOrderByCreateTime(OrderService orderService) {
        ListOrdersResult result = orderService.getListOrdersResponseByCreateTimeLastDay().getListOrdersResult();
        processOrderList(orderService, result.getOrders());
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            ListOrdersByNextTokenResult nextResult = orderService.getListOrdersByNextTokenResponse(nextToken).getListOrdersByNextTokenResult();
            processOrderList(orderService, nextResult.getOrders());
            nextToken = nextResult.getNextToken();
        }
    }

    private void fetchOrderByUpdateTime(OrderService orderService) {
        ListOrdersResult result = orderService.getListOrdersResponseByUpdateTimeWithinDays(orderRetrospectDays).getListOrdersResult();
        processOrderList(orderService, result.getOrders());
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            ListOrdersByNextTokenResult nextResult = orderService.getListOrdersByNextTokenResponse(nextToken).getListOrdersByNextTokenResult();
            processOrderList(orderService, nextResult.getOrders());
            nextToken = nextResult.getNextToken();
        }
    }

    private void fetchOrderItem(OrderService orderService, AmazonOrder order) {
        ListOrderItemsResult result = orderService.getListOrderItemsResponse(order.getAmazonOrderId()).getListOrderItemsResult();
        result.getOrderItems().forEach(item -> saveOrderItem(order, item));
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            ListOrderItemsByNextTokenResult nextResult = orderService.getListOrderItemsByNextTokenResponse(nextToken).getListOrderItemsByNextTokenResult();
            nextResult.getOrderItems().forEach(item -> saveOrderItem(order, item));
            nextToken = nextResult.getNextToken();
        }
    }

    private void processOrderList(OrderService orderService, List<Order> list) {
        for (Order order : list) {
            AmazonOrder amazonOrder = saveOrder(orderService.getMws().getMarketplace(), order);
            if (amazonOrder != null) {
                fetchOrderItem(orderService, amazonOrder);
            }
        }
    }

    private AmazonOrder saveOrder(String market, Order order) {
        AmazonOrder amazonOrder = amazonOrderRepository.findByAmazonOrderId(order.getAmazonOrderId());
        if (amazonOrder == null) {
            LOGGER.info("{} saving order: {}, {}", market, order.getAmazonOrderId(), order.getOrderStatus());
            amazonOrder = new AmazonOrder();
            amazonOrder.setMarket(market);
            amazonOrder.setAmazonOrderId(order.getAmazonOrderId());
            amazonOrder.setStatus(order.getOrderStatus());
            amazonOrder.setFulfillment(order.getFulfillmentChannel());
            amazonOrder.setData(Utils.getJsonString(order));
            amazonOrder.setPurchaseDate(new Date(order.getPurchaseDate().toGregorianCalendar().getTimeInMillis()));
            amazonOrder.setCreateTime(new Timestamp(System.currentTimeMillis()));
            amazonOrderRepository.save(amazonOrder);
            return amazonOrder;
        } else {
            // already existed
            return null;
        }
    }

    private void saveOrderItem(AmazonOrder order, OrderItem orderItem) {
        String market = order.getMarket();
        String sellerSku = orderItem.getSellerSKU();
        LOGGER.info("{} saving orderItem: {}, {}", market, order.getAmazonOrderId(), orderItem.getOrderItemId());
        Map<String, String> map = Utils.parseSellerSku(sellerSku);
        AmazonOrderItem amazonOrderItem = new AmazonOrderItem();
        amazonOrderItem.setMarket(market);
        amazonOrderItem.setAmazonOrderId(order.getAmazonOrderId());
        amazonOrderItem.setStatus(order.getStatus());
        amazonOrderItem.setFulfillment(order.getFulfillment());
        amazonOrderItem.setPurchaseDate(order.getPurchaseDate());
        amazonOrderItem.setAmazonOrderItemId(orderItem.getOrderItemId());
        amazonOrderItem.setQuantity(orderItem.getQuantityOrdered());
        amazonOrderItem.setAsin(orderItem.getASIN());
        amazonOrderItem.setSellerSku(sellerSku);
        amazonOrderItem.setSku(map.get("sku"));
        amazonOrderItem.setSize(map.get("size"));
        amazonOrderItem.setData(Utils.getJsonString(orderItem));
        amazonOrderItem.setCreateTime(new Timestamp(System.currentTimeMillis()));
        amazonOrderItemRepository.save(amazonOrderItem);
        try {
            AmazonSellerSku amazonSellerSku = amazonSellerSkuRepository.findByMarketAndSellerSku(market, sellerSku);
            if (amazonSellerSku == null) {
                amazonSellerSku = new AmazonSellerSku();
                amazonSellerSku.setMarket(market);
                amazonSellerSku.setSellerSku(sellerSku);
                amazonSellerSku.setSku(amazonOrderItem.getSku());
                amazonSellerSku.setSize(amazonOrderItem.getSize());
                amazonSellerSku.setCreateTime(amazonOrderItem.getCreateTime());
                amazonSellerSkuRepository.save(amazonSellerSku);
            }
        } catch (Throwable t) {
            LOGGER.info("{} saving seller sku error: {}", market, t.getMessage(), t);
        }
    }

    private void processInboundShipmentInfo(InboundShipmentInfo info, String market, InboundService inboundService) {
        String shipmentId = info.getShipmentId();
        String status = info.getShipmentStatus();
        AmazonInbound inbound = amazonInboundRepository.findByShipmentId(shipmentId);
        if (inbound == null) {
            LOGGER.info("Saving shipment {}, status {}", info.getShipmentName(), status);
            inbound = new AmazonInbound(info);
            inbound.setMarket(market);
            amazonInboundRepository.save(inbound);
            ListInboundShipmentItemsResult result = inboundService
                    .getListInboundShipmentItemsResponse(shipmentId)
                    .getListInboundShipmentItemsResult();
            for (InboundShipmentItem item : result.getItemData().getMember()) {
                saveInboundShipmentItem(inbound, item);
            }
            String token = result.getNextToken();
            while (StringUtils.isNotEmpty(token)) {
                ListInboundShipmentItemsByNextTokenResult nextResult = inboundService
                        .getListInboundShipmentItemsByNextTokenResponse(token)
                        .getListInboundShipmentItemsByNextTokenResult();
                for (InboundShipmentItem item : nextResult.getItemData().getMember()) {
                    saveInboundShipmentItem(inbound, item);
                }
                token = nextResult.getNextToken();
            }
        } else {
            if (!inbound.getStatus().equals(status)) {
                LOGGER.info("Updating shipment {}, status {}", info.getShipmentName(), status);
                inbound.setStatus(status);
                amazonInboundRepository.save(inbound);
                updateInboundShipmentItemStatus(shipmentId, status);
            }
        }
    }

    private void saveInboundShipmentItem(AmazonInbound inbound, InboundShipmentItem item) {
        LOGGER.info("Saving shipment item {}, status {}", item.getSellerSKU(), inbound.getStatus());
        AmazonInboundItem amazonInboundItem = new AmazonInboundItem(inbound, item);
        Map<String, String> map = Utils.parseSellerSku(item.getSellerSKU());
        amazonInboundItem.setSku(map.get("sku"));
        amazonInboundItem.setSize(map.get("size"));
        amazonInboundItemRepository.save(amazonInboundItem);
    }

    private void updateInboundShipmentItemStatus(String shipmentId, String status) {
        Iterable<AmazonInboundItem> iterable = amazonInboundItemRepository.findAllByShipmentId(shipmentId);
        for (AmazonInboundItem item : iterable) {
            LOGGER.info("Updating shipment item {}, status {}", item.getSellerSku(), status);
            item.setStatus(status);
            amazonInboundItemRepository.save(item);
        }
    }
}
