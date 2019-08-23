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
import com.onlymaker.scorpio.mws.InboundService;
import com.onlymaker.scorpio.mws.OrderService;
import com.onlymaker.scorpio.mws.ReportService;
import com.onlymaker.scorpio.mws.Utils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AmazonFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonFetcher.class);
    @Value("${fetcher.order.retrospect.days}")
    Long orderRetrospectDays;
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;
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

    @Scheduled(cron = "${fetcher.mws}")
    public void everyday() {
        LOGGER.info("run fetcher ...");
        requestInventoryReport();
        requestReceiptReport();
        fetchOrder();
        fetchInbound();
        fetchInventoryReport();
        fetchReceiptReport();
    }

    private void fetchOrder() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            OrderService orderService = new OrderService(appInfo, mws);
            try {
                fetchOrderByCreateTime(orderService);
            } catch (Throwable t) {
                LOGGER.error("{} fetch order unexpected error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
            try {
                fetchOrderByUpdateTime(orderService);
            } catch (Throwable t) {
                LOGGER.error("{} fetch order unexpected error: {}", mws.getMarketplace(), t.getMessage(), t);
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
        requestReport(ReportService.REPORT_TYPE.get("inventory"));
    }

    private void requestReceiptReport() {
        requestReport(ReportService.REPORT_TYPE.get("receipt"));
    }

    private void requestReport(String type) {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            ReportService reportService = new ReportService(appInfo, mws);
            try {
                RequestReportResponse requestReportResponse = reportService.requestReport(type);
                ReportRequestInfo requestInfo = requestReportResponse.getRequestReportResult().getReportRequestInfo();
                LOGGER.info("Request {}, id: {}", type, requestInfo.getReportRequestId());
                AmazonReportLog amazonReportLog = new AmazonReportLog();
                amazonReportLog.setRequestId(requestInfo.getReportRequestId());
                String generatedReportId = requestInfo.getGeneratedReportId();
                amazonReportLog.setReportId(generatedReportId == null ? "" : generatedReportId);
                amazonReportLog.setReportType(type);
                amazonReportLog.setStatus(0);
                amazonReportLog.setCreateTime(new Timestamp(requestInfo.getSubmittedDate().toGregorianCalendar().getTimeInMillis()));
                amazonReportLogRepository.save(amazonReportLog);
            } catch (Throwable t) {
                LOGGER.error("{} request {} error: {}", mws.getMarketplace(), type, t.getMessage(), t);
            }
        }
    }

    private void fetchInventoryReport() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            ReportService reportService = new ReportService(appInfo, mws);
            try {
                Map<String, Integer> fields = new HashMap<>();
                File report = new File("/tmp/inventory");
                List<String> lines = fetchReport(reportService, ReportService.REPORT_TYPE.get("inventory"), report);
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
                        String sku = "";
                        String size = "";
                        AmazonSellerSku amazonSellerSku = saveAmazonSellerSku(market, sellerSku);
                        if (amazonSellerSku != null) {
                            sku = amazonSellerSku.getSku();
                            size = amazonSellerSku.getSize();
                        }
                        Date date = new Date(System.currentTimeMillis());
                        AmazonInventory amazonInventory = amazonInventoryRepository.findByMarketAndFnSkuAndCreateDate(market, fnSku, date);
                        if (amazonInventory == null) {
                            LOGGER.info("{} saving inventory: {}", market, sellerSku);
                            amazonInventory = new AmazonInventory();
                            amazonInventory.setMarket(market);
                            amazonInventory.setFnSku(fnSku);
                            amazonInventory.setCreateDate(date);
                            amazonInventory.setAsin(elements[fields.get("asin")]);
                            amazonInventory.setSellerSku(sellerSku);
                            amazonInventory.setName(elements[fields.get("product-name")]);
                            amazonInventory.setSku(sku);
                            amazonInventory.setSize(size);
                            amazonInventory.setInStockQuantity(Integer.parseInt(elements[fields.get("afn-fulfillable-quantity")]));
                            amazonInventory.setTotalQuantity(Integer.parseInt(elements[fields.get("afn-total-quantity")]));
                            amazonInventoryRepository.save(amazonInventory);
                        }
                    }
                }
            } catch (Throwable t) {
                LOGGER.error("{} fetch inventory report error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
        }
    }

    private void fetchReceiptReport() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            ReportService reportService = new ReportService(appInfo, mws);
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Map<String, Integer> fields = new HashMap<>();
                File report = new File("/tmp/receipt");
                List<String> lines = fetchReport(reportService, ReportService.REPORT_TYPE.get("receipt"), report);
                for (String line : lines) {
                    LOGGER.debug("report line: {}", line);
                    String[] elements = line.split("\t");
                    //received-date	fnsku	sku	product-name	quantity	fba-shipment-id	fulfillment-center-id
                    if (line.startsWith("received-date")) {
                        for (int i = 0; i < elements.length; i++) {
                            fields.put(elements[i], i);
                        }
                    } else {
                        String receiveDate = elements[fields.get("received-date")];
                        String shipmentId = elements[fields.get("fba-shipment-id")];
                        LOGGER.info("{} receive date is {}", shipmentId, receiveDate);
                        AmazonInbound inbound = amazonInboundRepository.findByShipmentId(shipmentId);
                        if (inbound != null && inbound.getReceiveDate() == null) {
                            String[] datetime = receiveDate.split("T");
                            Date date = new Date(formatter.parse(datetime[0]).getTime());
                            inbound.setReceiveDate(date);
                            amazonInboundRepository.save(inbound);
                            Iterable<AmazonInboundItem> iterable = amazonInboundItemRepository.findAllByShipmentId(shipmentId);
                            for (AmazonInboundItem item : iterable) {
                                item.setReceiveDate(date);
                                amazonInboundItemRepository.save(item);
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                LOGGER.error("{} fetch receipt report error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
        }
    }

    private List<String> fetchReport(ReportService reportService, String type, File report) throws Exception {
        List<String> lines = new ArrayList<>();
        GetReportRequestListResponse reportRequestListResponse = reportService.getReportRequestListCompleted(type);
        List<ReportRequestInfo> reportRequestInfos = reportRequestListResponse.getGetReportRequestListResult().getReportRequestInfoList();
        for (ReportRequestInfo reportRequestInfo : reportRequestInfos) {
            AmazonReportLog log = amazonReportLogRepository.findOneByRequestIdAndStatus(reportRequestInfo.getReportRequestId(), 0);
            if (log != null) {
                // _CANCELLED_, _DONE_NO_DATA_ will return null generatedReportId
                if (reportRequestInfo.getReportProcessingStatus().equals("_DONE_")) {
                    String reportId = reportRequestInfo.getGeneratedReportId();
                    GetReportRequest request = reportService.prepareGetReport(reportId, new FileOutputStream(report));
                    reportService.getReport(request);
                    request.getReportOutputStream().close();
                    String encoding = UniversalDetector.detectCharset(report);
                    if (encoding != null) {
                        LOGGER.debug("report detect encoding: {}", encoding);
                        lines = Files.readAllLines(report.toPath(), Charset.forName(encoding));
                    } else {
                        LOGGER.debug("report default encoding: {}", Charset.defaultCharset());
                        lines = Files.readAllLines(report.toPath());
                    }
                    log.setReportId(reportId);
                }
                log.setStatus(1);
                amazonReportLogRepository.save(log);
            }
        }
        return lines;
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
        String sku = "";
        String size = "";
        AmazonSellerSku amazonSellerSku = saveAmazonSellerSku(market, sellerSku);
        if (amazonSellerSku != null) {
            sku = amazonSellerSku.getSku();
            size = amazonSellerSku.getSize();
        }
        LOGGER.info("{} saving orderItem: {}, {}", market, order.getAmazonOrderId(), orderItem.getOrderItemId());
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
        amazonOrderItem.setSku(sku);
        amazonOrderItem.setSize(size);
        amazonOrderItem.setData(Utils.getJsonString(orderItem));
        amazonOrderItem.setCreateTime(new Timestamp(System.currentTimeMillis()));
        amazonOrderItemRepository.save(amazonOrderItem);
    }

    private void processInboundShipmentInfo(InboundShipmentInfo info, String market, InboundService inboundService) {
        String shipmentId = info.getShipmentId();
        String status = info.getShipmentStatus();
        AmazonInbound inbound = amazonInboundRepository.findByShipmentId(shipmentId);
        if (inbound == null) {
            LOGGER.info("Saving shipment {}, status {}", info.getShipmentId(), status);
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
                LOGGER.info("Updating shipment {}, status {}", info.getShipmentId(), status);
                inbound.setShipmentName(info.getShipmentName());
                inbound.setStatus(status);
                amazonInboundRepository.save(inbound);
                updateInboundShipmentItemStatus(shipmentId, status);
            }
        }
    }

    private void saveInboundShipmentItem(AmazonInbound inbound, InboundShipmentItem shipment) {
        LOGGER.info("Saving shipment item {}, status {}", shipment.getSellerSKU(), inbound.getStatus());
        AmazonInboundItem item = new AmazonInboundItem(inbound, shipment);
        String sku = "";
        String size = "";
        AmazonSellerSku amazonSellerSku = saveAmazonSellerSku(item.getMarket(), item.getSellerSku());
        if (amazonSellerSku != null) {
            sku = amazonSellerSku.getSku();
            size = amazonSellerSku.getSize();
        }
        item.setSku(sku);
        item.setSize(size);
        amazonInboundItemRepository.save(item);
    }

    private void updateInboundShipmentItemStatus(String shipmentId, String status) {
        Iterable<AmazonInboundItem> iterable = amazonInboundItemRepository.findAllByShipmentId(shipmentId);
        for (AmazonInboundItem item : iterable) {
            LOGGER.info("Updating shipment item {}, status {}", item.getSellerSku(), status);
            item.setStatus(status);
            amazonInboundItemRepository.save(item);
        }
    }

    private AmazonSellerSku saveAmazonSellerSku(String market, String sellerSku) {
        try {
            AmazonSellerSku amazonSellerSku = amazonSellerSkuRepository.findByMarketAndSellerSku(market, sellerSku);
            if (amazonSellerSku == null) {
                Map<String, String> map = Utils.parseSellerSku(sellerSku);
                String sku = map.get("sku");
                String size = map.get("size");
                LOGGER.info("{} saving seller sku {}: {} {}", market, sellerSku, sku, size);
                amazonSellerSku = new AmazonSellerSku();
                amazonSellerSku.setMarket(market);
                amazonSellerSku.setSellerSku(sellerSku);
                amazonSellerSku.setSku(sku);
                amazonSellerSku.setSize(size);
                amazonSellerSku.setCreateTime(new Timestamp(System.currentTimeMillis()));
                amazonSellerSkuRepository.save(amazonSellerSku);
            }
            return amazonSellerSku;
        } catch (Throwable t) {
            LOGGER.error("{} saving seller sku error: {}", market, t.getMessage(), t);
        }
        return null;
    }
}
