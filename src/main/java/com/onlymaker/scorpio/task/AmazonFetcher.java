package com.onlymaker.scorpio.task;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.InventorySupply;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.ListInventorySupplyByNextTokenResult;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.ListInventorySupplyRequest;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.ListInventorySupplyResponse;
import com.amazonservices.mws.orders._2013_09_01.model.*;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;
import com.onlymaker.scorpio.data.*;
import com.onlymaker.scorpio.mws.HtmlPageService;
import com.onlymaker.scorpio.mws.InventoryService;
import com.onlymaker.scorpio.mws.OrderService;
import com.onlymaker.scorpio.mws.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
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

    @Scheduled(cron = "${fetcher.cron}")
    public void everyday() {
        LOGGER.info("run fetch task ...");
        fetchHtml();
        fetchMwsOrder();
        fetchMwsInventory();
    }

    private void fetchHtml() {
        String lastAsin = "";
        for (AmazonEntry entry : amazonEntryRepository.findAllByStatusOrderByAsin(AmazonEntry.STATUS_ENABLED)) {
            try {
                if (!Objects.equals(lastAsin, entry.getAsin())) {
                    AmazonEntrySnapshot snapshot = htmlPageService.parse(entry);
                    amazonEntrySnapshotRepository.save(snapshot);
                    lastAsin = entry.getAsin();
                    Thread.sleep(SECOND_IN_MS);
                }
            } catch (Throwable t) {
                LOGGER.info("fetch html ({}) unexpected error: {}", entry.getAsin(), t, t.getMessage(), t);
            }
        }
    }

    private void fetchMwsOrder() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            OrderService orderService = new OrderService(appInfo, mws);
            try {
                fetchOrder(orderService);
            } catch (Throwable t) {
                LOGGER.info("fetch order ({}) unexpected error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
            try {
                updateOrder(orderService);
            } catch (Throwable t) {
                LOGGER.info("fetch order ({}) unexpected error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
        }
    }

    private void fetchMwsInventory() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            InventoryService inventoryService = new InventoryService(appInfo, mws);
            try {
                fetchInventory(inventoryService);
            } catch (Throwable t) {
                LOGGER.info("fetch inventory ({}) unexpected error: {}", mws.getMarketplace(), t.getMessage(), t);
            }
        }
    }

    private void fetchOrder(OrderService orderService) {
        ListOrdersResult result = orderService.getListOrdersResponseByCreateTimeLastDay().getListOrdersResult();
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

    private void updateOrder(OrderService orderService) {
        ListOrdersResult result = orderService.getListOrdersResponseByUpdateTimeWithinDays(orderRetrospectDays).getListOrdersResult();
        processOrderList(orderService, result.getOrders());
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            ListOrdersByNextTokenResult nextResult = orderService.getListOrdersByNextTokenResponse(nextToken).getListOrdersByNextTokenResult();
            processOrderList(orderService, nextResult.getOrders());
            nextToken = nextResult.getNextToken();
        }
    }

    private void fetchInventory(InventoryService inventoryService) {
        ListInventorySupplyRequest request = inventoryService.buildRequestWithinLastDay();
        ListInventorySupplyResponse response = inventoryService.getListInventorySupplyResponse(request);
        processInventoryList(inventoryService.getMws().getMarketplace(), response.getListInventorySupplyResult().getInventorySupplyList().getMember());
        String nextToken = response.getListInventorySupplyResult().getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            ListInventorySupplyByNextTokenResult nextResult = inventoryService.getListInventorySupplyByNextTokenResponse(nextToken).getListInventorySupplyByNextTokenResult();
            processInventoryList(inventoryService.getMws().getMarketplace(), nextResult.getInventorySupplyList().getMember());
            nextToken = nextResult.getNextToken();
        }
    }

    private void processOrderList(OrderService orderService, List<Order> list) {
        for (Order order : list) {
            AmazonOrder amazonOrder = saveOrUpdate(orderService.getMws().getMarketplace(), order);
            if (amazonOrder.getCreateTime().getTime() + orderService.getFetchOrderItemIntervalInMs() > System.currentTimeMillis()) {
                fetchOrderItem(orderService, amazonOrder);
            }
        }
    }

    private void processInventoryList(String market, List<InventorySupply> list) {
        for (InventorySupply inventorySupply : list) {
            String fnSku = inventorySupply.getFNSKU();
            AmazonInventory amazonInventory = amazonInventoryRepository.findByMarketAndFnSkuAndCreateDate(market, fnSku, new Date(System.currentTimeMillis()));
            if (amazonInventory == null) {
                LOGGER.info("saving inventory {}: {}", market, fnSku);
                amazonInventory = new AmazonInventory();
                amazonInventory.setMarket(market);
                amazonInventory.setFnSku(fnSku);
                amazonInventory.setCreateDate(new Date(System.currentTimeMillis()));
            } else {
                LOGGER.info("updating inventory {}: {}", market, fnSku);
            }
            amazonInventory.setAsin(inventorySupply.getASIN());
            amazonInventory.setSellerSku(inventorySupply.getSellerSKU());
            amazonInventory.setInStockQuantity(inventorySupply.getInStockSupplyQuantity());
            amazonInventory.setTotalQuantity(inventorySupply.getTotalSupplyQuantity());
            amazonInventory.setFulfillment(inventorySupply.getSellerSKU().contains("FBA") ? Utils.FULFILL_BY_FBA : Utils.FULFILL_NOT_FBA);
            amazonInventoryRepository.save(amazonInventory);
        }
    }

    private AmazonOrder saveOrUpdate(String market, Order order) {
        AmazonOrder amazonOrder = amazonOrderRepository.findByAmazonOrderId(order.getAmazonOrderId());
        if (amazonOrder == null){
            LOGGER.info("saving order {}: {}", order.getAmazonOrderId(), order.getOrderStatus());
            amazonOrder = new AmazonOrder();
            amazonOrder.setMarket(market);
            amazonOrder.setAmazonOrderId(order.getAmazonOrderId());
            amazonOrder.setStatus(order.getOrderStatus());
            amazonOrder.setFulfillment(order.getFulfillmentChannel());
            amazonOrder.setData(Utils.getJsonString(order));
            amazonOrder.setPurchaseDate(new Date(order.getPurchaseDate().toGregorianCalendar().getTimeInMillis()));
            amazonOrder.setCreateTime(new Timestamp(System.currentTimeMillis()));
            amazonOrderRepository.save(amazonOrder);
        } else if (!Objects.equals(amazonOrder.getStatus(), order.getOrderStatus())) {
            LOGGER.info("updating order {}: {}", order.getAmazonOrderId(), order.getOrderStatus());
            amazonOrder.setStatus(order.getOrderStatus());
            amazonOrder.setData(Utils.getJsonString(order));
            amazonOrder.setPurchaseDate(new Date(order.getPurchaseDate().toGregorianCalendar().getTimeInMillis()));
            amazonOrderRepository.save(amazonOrder);
        }
        return amazonOrder;
    }

    private void saveOrderItem(AmazonOrder order, OrderItem orderItem) {
        LOGGER.info("saving orderItem {} of {}", orderItem.getOrderItemId(), order.getAmazonOrderId());
        AmazonOrderItem amazonOrderItem = new AmazonOrderItem();
        amazonOrderItem.setMarket(order.getMarket());
        amazonOrderItem.setAmazonOrderId(order.getAmazonOrderId());
        amazonOrderItem.setStatus(order.getStatus());
        amazonOrderItem.setFulfillment(order.getFulfillment());
        amazonOrderItem.setPurchaseDate(order.getPurchaseDate());
        amazonOrderItem.setAmazonOrderItemId(orderItem.getOrderItemId());
        amazonOrderItem.setQuantity(orderItem.getQuantityOrdered());
        amazonOrderItem.setAsin(orderItem.getASIN());
        amazonOrderItem.setSellerSku(orderItem.getSellerSKU());
        amazonOrderItem.setData(Utils.getJsonString(orderItem));
        amazonOrderItem.setCreateTime(new Timestamp(System.currentTimeMillis()));
        amazonOrderItemRepository.save(amazonOrderItem);
    }
}
