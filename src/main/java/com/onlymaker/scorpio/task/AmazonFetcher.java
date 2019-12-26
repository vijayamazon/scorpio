package com.onlymaker.scorpio.task;

import com.amazonaws.mws.model.GetReportListResponse;
import com.amazonaws.mws.model.ReportInfo;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    AmazonFBAReturnRepository amazonFBAReturnRepository;
    @Autowired
    AmazonInboundRepository amazonInboundRepository;
    @Autowired
    AmazonInboundItemRepository amazonInboundItemRepository;
    @Autowired
    AmazonEntryRepository amazonEntryRepository;
    @Autowired
    AmazonProductRepository amazonProductRepository;
    @Autowired
    AmazonAgeRepository amazonAgeRepository;

    @Scheduled(cron = "${fetcher.mws}")
    public void run() {
        LOGGER.info("run fetcher ...");
        fetchInventoryReport();
        fetchFbaReturnReport();
        fetchAgeReport();
        fetchReceiptReport();
        fetchOrder();
        fetchInbound();
        requestReport(ReportService.REPORT_TYPE.get("inventory"));
        requestReport(ReportService.REPORT_TYPE.get("receipt"));
        requestReport(ReportService.REPORT_TYPE.get("fba_return"));
        requestReport(ReportService.REPORT_TYPE.get("age"));
        fetchProduct();
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

    private void requestReport(String type) {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            ReportService reportService = new ReportService(appInfo, mws);
            try {
                RequestReportResponse requestReportResponse = reportService.requestReport(type);
                ReportRequestInfo requestInfo = requestReportResponse.getRequestReportResult().getReportRequestInfo();
                LOGGER.info("Request {}, id: {}", type, requestInfo.getReportRequestId());
            } catch (Throwable t) {
                LOGGER.error("{} request {} error: {}", mws.getMarketplace(), type, t.getMessage(), t);
            }
        }
    }

    private String getLatestReportId(MarketWebService mws, String type) {
        ReportService reportService = new ReportService(appInfo, mws);
        try {
            GetReportListResponse response = reportService.getReportList(type);
            List<ReportInfo> list = response.getGetReportListResult().getReportInfoList();
            if (!list.isEmpty()) {
                ReportInfo latest = list.get(0);
                return latest.getReportId();
            }
        } catch (Throwable t) {
            LOGGER.error("{} request reportList {} error: {}", mws.getMarketplace(), type, t.getMessage(), t);
        }
        return null;
    }

    private void fetchInventoryReport() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            ReportService reportService = new ReportService(appInfo, mws);
            String id = getLatestReportId(mws, ReportService.REPORT_TYPE.get("inventory"));
            if (StringUtils.isNotEmpty(id)) {
                try {
                    Map<String, Integer> fields = new HashMap<>();
                    File report = new File("/tmp/inventory_" + mws.getMarketplace());
                    List<String> lines = reportService.getReportContent(id, report);
                    for (String line : lines) {
                        //sku	fnsku	asin	product-name	condition	your-price	mfn-listing-exists	mfn-fulfillable-quantity	afn-listing-exists	afn-warehouse-quantity	afn-fulfillable-quantity	afn-unsellable-quantity	afn-reserved-quantity	afn-total-quantity	per-unit-volume	afn-inbound-working-quantity	afn-inbound-shipped-quantity	afn-inbound-receiving-quantity
                        String[] elements = reportService.splitReportLine(line);
                        if (line.startsWith("sku")) {
                            for (int i = 0; i < elements.length; i++) {
                                fields.put(elements[i], i);
                            }
                        } else {
                            String market = mws.getMarketplace();
                            String asin = elements[fields.get("asin")];
                            String sellerSku = elements[fields.get("sku")];
                            Map<String, String> map = Utils.parseSellerSku(sellerSku);
                            Date date = new Date(System.currentTimeMillis());
                            AmazonInventory amazonInventory = amazonInventoryRepository.findByMarketAndAsinAndCreateDate(market, asin, date);
                            if (amazonInventory == null) {
                                amazonInventory = new AmazonInventory();
                            }
                            LOGGER.info("{} saving inventory: {}", market, sellerSku);
                            amazonInventory.setMarket(market);
                            amazonInventory.setAsin(asin);
                            amazonInventory.setCreateDate(date);
                            amazonInventory.setFnSku(elements[fields.get("fnsku")]);
                            amazonInventory.setSellerSku(sellerSku);
                            amazonInventory.setName(elements[fields.get("product-name")]);
                            amazonInventory.setSku(map.get("sku"));
                            amazonInventory.setSize(map.get("size"));
                            amazonInventory.setInStockQuantity(Integer.parseInt(elements[fields.get("afn-fulfillable-quantity")]));
                            amazonInventory.setTotalQuantity(Integer.parseInt(elements[fields.get("afn-total-quantity")]));
                            amazonInventory.setData(line);
                            amazonInventoryRepository.save(amazonInventory);
                            updateProduct(market, asin, sellerSku, amazonInventory.getSku(), amazonInventory.getSize());
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.error("{} fetch inventory report error: {}", mws.getMarketplace(), t.getMessage(), t);
                }
            }
        }
    }

    private void fetchFbaReturnReport() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            ReportService reportService = new ReportService(appInfo, mws);
            String id = getLatestReportId(mws, ReportService.REPORT_TYPE.get("fba_return"));
            if (StringUtils.isNotEmpty(id)) {
                try {
                    SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Map<String, Integer> fields = new HashMap<>();
                    File report = new File("/tmp/fba_return_" + mws.getMarketplace());
                    List<String> lines = reportService.getReportContent(id, report);
                    for (String line : lines) {
                        //"return-date","order-id","sku","asin","fnsku","product-name","quantity","fulfillment-center-id","detailed-disposition","reason","status","license-plate-number","customer-comments"
                        String[] elements = reportService.splitReportLine(line);
                        if (fields.isEmpty()) {
                            for (int i = 0; i < elements.length; i++) {
                                fields.put(elements[i], i);
                            }
                        } else {
                            String market = mws.getMarketplace();
                            String returnDate = elements[fields.get("return-date")];
                            Date date = new Date(f1.parse(returnDate.split("T")[0]).getTime());
                            Timestamp time = new Timestamp(f2.parse((returnDate.split("\\+")[0])).getTime());
                            String orderId = elements[fields.get("order-id")];
                            String asin = elements[fields.get("asin")];
                            String sellerSku = elements[fields.get("sku")];
                            Map<String, String> map = Utils.parseSellerSku(sellerSku);
                            AmazonFBAReturn amazonFBAReturn = amazonFBAReturnRepository.findOneByMarketAndTimeAndOrderIdAndAsin(market, time, orderId, asin);
                            if (amazonFBAReturn == null) {
                                amazonFBAReturn = new AmazonFBAReturn();
                                amazonFBAReturn.setMarket(market);
                                amazonFBAReturn.setDate(date);
                                amazonFBAReturn.setTime(time);
                                amazonFBAReturn.setOrderId(orderId);
                                amazonFBAReturn.setAsin(asin);
                            }
                            LOGGER.info("{} saving fba return: {}", market, sellerSku);
                            amazonFBAReturn.setSellerSku(sellerSku);
                            amazonFBAReturn.setSku(map.get("sku"));
                            amazonFBAReturn.setSize(map.get("size"));
                            amazonFBAReturn.setFnSku(elements[fields.get("fnsku")]);
                            amazonFBAReturn.setProductName(elements[fields.get("product-name")]);
                            amazonFBAReturn.setQuantity(Integer.parseInt(elements[fields.get("quantity")]));
                            amazonFBAReturn.setFulfillmentCenterId(elements[fields.get("fulfillment-center-id")]);
                            amazonFBAReturn.setDetailedDisposition(elements[fields.get("detailed-disposition")]);
                            amazonFBAReturn.setReason(elements[fields.get("reason")]);
                            amazonFBAReturn.setStatus(elements[fields.get("status")]);
                            amazonFBAReturn.setLicensePlateNumber(elements[fields.get("license-plate-number")]);
                            amazonFBAReturn.setCustomerComments(elements[fields.get("customer-comments")]);
                            amazonFBAReturnRepository.save(amazonFBAReturn);
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.error("{} fetch fba return report error: {}", mws.getMarketplace(), t.getMessage(), t);
                }
            }
        }
    }

    private void fetchAgeReport() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            ReportService reportService = new ReportService(appInfo, mws);
            String id = getLatestReportId(mws, ReportService.REPORT_TYPE.get("age"));
            if (StringUtils.isNotEmpty(id)) {
                try {
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
                    Map<String, Integer> fields = new HashMap<>();
                    File report = new File("/tmp/age_" + mws.getMarketplace());
                    List<String> lines = reportService.getReportContent(id, report);
                    for (String line : lines) {
                        //"snapshot-date","sku","fnsku","asin","product-name","condition","avaliable-quantity(sellable)","qty-with-removals-in-progress","inv-age-0-to-90-days","inv-age-91-to-180-days","inv-age-181-to-270-days","inv-age-271-to-365-days","inv-age-365-plus-days","currency","qty-to-be-charged-ltsf-6-mo","projected-ltsf-6-mo","qty-to-be-charged-ltsf-12-mo","projected-ltsf-12-mo","units-shipped-last-7-days","units-shipped-last-30-days","units-shipped-last-60-days","units-shipped-last-90-days","alert","your-price","sales_price","lowest_price_new","lowest_price_used","Recommended action","Healthy Inventory Level","Recommended sales price","Recommended sale duration (days)","Recommended Removal Quantity","Estimated cost savings of removal","sell-through","cubic-feet","storage-type"
                        String[] elements = reportService.splitReportLine(line);
                        if (fields.isEmpty()) {
                            for (int i = 0; i < elements.length; i++) {
                                fields.put(elements[i], i);
                            }
                        } else {
                            String market = mws.getMarketplace();
                            String snapshotDate = elements[fields.get("snapshot-date")];
                            Date date = new Date(f.parse(snapshotDate).getTime());
                            String asin = elements[fields.get("asin")];
                            String sellerSku = elements[fields.get("sku")];
                            Map<String, String> map = Utils.parseSellerSku(sellerSku);
                            AmazonAge amazonAge = amazonAgeRepository.findByMarketAndAsin(market, asin);
                            if (amazonAge == null) {
                                amazonAge = new AmazonAge();
                                amazonAge.setMarket(market);
                                amazonAge.setAsin(asin);
                            }
                            LOGGER.info("{} saving age: {}", market, sellerSku);
                            amazonAge.setDate(date);
                            amazonAge.setFnSku(elements[fields.get("fnsku")]);
                            amazonAge.setSellerSku(sellerSku);
                            amazonAge.setSku(map.get("sku"));
                            amazonAge.setSize(map.get("size"));
                            amazonAge.setCurrency(elements[fields.get("currency")]);
                            amazonAge.setQuantity(Integer.parseInt(elements[fields.get("avaliable-quantity(sellable)")]));
                            amazonAge.setAge90(Integer.parseInt(elements[fields.get("inv-age-0-to-90-days")]));
                            amazonAge.setAge180(Integer.parseInt(elements[fields.get("inv-age-91-to-180-days")]));
                            amazonAge.setAge270(Integer.parseInt(elements[fields.get("inv-age-181-to-270-days")]));
                            amazonAge.setAge365(Integer.parseInt(elements[fields.get("inv-age-271-to-365-days")]));
                            amazonAge.setAgeYearPlus(Integer.parseInt(elements[fields.get("inv-age-365-plus-days")]));
                            amazonAgeRepository.save(amazonAge);
                            updateProduct(market, asin, sellerSku, amazonAge.getSku(), amazonAge.getSize());
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.error("{} fetch age report error: {}", mws.getMarketplace(), t.getMessage(), t);
                }
            }
        }
    }

    private void fetchReceiptReport() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            ReportService reportService = new ReportService(appInfo, mws);
            String id = getLatestReportId(mws, ReportService.REPORT_TYPE.get("receipt"));
            if (StringUtils.isNotEmpty(id)) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Map<String, Integer> fields = new HashMap<>();
                    File report = new File("/tmp/receipt_" + mws.getMarketplace());
                    List<String> lines = reportService.getReportContent(id, report);
                    for (String line : lines) {
                        //received-date	fnsku	sku	product-name	quantity	fba-shipment-id	fulfillment-center-id
                        String[] elements = reportService.splitReportLine(line);
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
    }

    private void fetchProduct() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            String market = mws.getMarketplace();
            ProductService productService = new ProductService(appInfo, mws);
            try {
                amazonEntryRepository.findByMarketAndStatus(market, 0).forEach(entry -> {
                    String parent = entry.getAsin();
                    LOGGER.info(market + " asin: " + parent);
                    Map<String, Map<String, String>> map = productService.getProductInfo(parent);
                    if (map == null) {
                        entry.setStatus(AmazonEntry.STATUS_INVALID);
                        amazonEntryRepository.save(entry);
                    } else {
                        map.forEach(((child, info) -> {
                            LOGGER.info(market + " child: " + child);
                            AmazonProduct product = amazonProductRepository.findByMarketAndAsin(market, child);
                            if (product == null) {
                                product = new AmazonProduct();
                                product.setMarket(market);
                                product.setAsin(child);
                                String sellerSku = info.get("Model");
                                if (StringUtils.isNotEmpty(sellerSku)) {
                                    Map<String, String> result = Utils.parseSellerSku(sellerSku);
                                    product.setSellerSku(sellerSku);
                                    product.setSku(result.get("sku"));
                                    product.setSize(result.get("size"));
                                } else {
                                    product.setSize(info.get("Size"));
                                }
                            }
                            product.setParent(parent);
                            product.setTitle(info.get("Title"));
                            product.setImage(info.get("URL"));
                            product.setColor(info.get("Color"));
                            amazonProductRepository.save(product);
                        }));
                    }
                });
            } catch (Throwable t) {
                LOGGER.error("{} fetch product error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
            LOGGER.info(market + " product finish");
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
        Map<String, String> map = Utils.parseSellerSku(sellerSku);
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
        amazonOrderItem.setSku(map.get("sku"));
        amazonOrderItem.setSize(map.get("size"));
        amazonOrderItem.setData(Utils.getJsonString(orderItem));
        amazonOrderItem.setCreateTime(new Timestamp(System.currentTimeMillis()));
        amazonOrderItemRepository.save(amazonOrderItem);
        updateProduct(market, amazonOrderItem.getAsin(), sellerSku, amazonOrderItem.getSku(), amazonOrderItem.getSize());
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
        Map<String, String> map = Utils.parseSellerSku(item.getSellerSku());
        item.setSku(map.get("sku"));
        item.setSize(map.get("size"));
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

    private void updateProduct(String market, String asin, String sellerSku, String sku, String size) {
        if (StringUtils.isNotEmpty(sellerSku) && StringUtils.isNotEmpty(sku)) {
            AmazonProduct product = amazonProductRepository.findByMarketAndAsin(market, asin);
            if (product != null && !Objects.equals(product.getSellerSku(), sellerSku)) {
                product.setSellerSku(sellerSku);
                product.setSku(sku);
                product.setSize(size);
                amazonProductRepository.save(product);
            }
        }
    }
}
