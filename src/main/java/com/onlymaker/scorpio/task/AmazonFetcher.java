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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Service
public class AmazonFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonFetcher.class);
    private static final long SECOND_IN_MS = 1000;
    private static final long INIT_DELAY = 30 * SECOND_IN_MS;
    private static final long FIX_DELAY = 3600 * SECOND_IN_MS;
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

    @Scheduled(initialDelay = INIT_DELAY, fixedDelay = FIX_DELAY)
    public void everyday() {
        LOGGER.info("run fetch task ...");
        fetchHtml();
        fetchMwsInventory();
        fetchMwsOrder();
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
                LOGGER.info("fetch order ({}) unexpected error: {}", mws.getStore(), t.getMessage(), t);
            }
            try {
                updateOrder(orderService);
            } catch (Throwable t) {
                LOGGER.info("fetch order ({}) unexpected error: {}", mws.getStore(), t.getMessage(), t);
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
                LOGGER.info("fetch inventory ({}) unexpected error: {}", mws.getStore(), t.getMessage(), t);
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

    private void fetchOrderItem(OrderService orderService, String amazonOrderId) {
        ListOrderItemsResult result = orderService.getListOrderItemsResponse(amazonOrderId).getListOrderItemsResult();
        result.getOrderItems().forEach(o -> saveOrderItem(orderService.getMws().getStore(), orderService.getMws(). getMarketplace(), amazonOrderId, o));
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            ListOrderItemsByNextTokenResult nextResult = orderService.getListOrderItemsByNextTokenResponse(nextToken).getListOrderItemsByNextTokenResult();
            nextResult.getOrderItems().forEach(o -> saveOrderItem(orderService.getMws().getStore(), orderService.getMws().getMarketplace(), amazonOrderId, o));
            nextToken = nextResult.getNextToken();
        }
    }

    private void updateOrder(OrderService orderService) {
        ListOrdersResult result = orderService.getListOrdersResponseByUpdateTimeLast30Days().getListOrdersResult();
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
        processInventoryList(inventoryService.getMws().getStore(), inventoryService.getMws().getMarketplace(), response.getListInventorySupplyResult().getInventorySupplyList().getMember());
        String nextToken = response.getListInventorySupplyResult().getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            ListInventorySupplyByNextTokenResult nextResult = inventoryService.getListInventorySupplyByNextTokenResponse(nextToken).getListInventorySupplyByNextTokenResult();
            processInventoryList(inventoryService.getMws().getStore(), inventoryService.getMws().getMarketplace(), nextResult.getInventorySupplyList().getMember());
            nextToken = nextResult.getNextToken();
        }
    }

    private void processOrderList(OrderService orderService, List<Order> list) {
        for (Order order : list) {
            if (saveOrUpdate(orderService.getMws().getStore(), orderService.getMws().getMarketplace(), order)) {
                fetchOrderItem(orderService, order.getAmazonOrderId());
            }
        }
    }

    private void processInventoryList(String store, String market, List<InventorySupply> list) {
        for (InventorySupply inventorySupply : list) {
            String asin = inventorySupply.getASIN();
            String sellerSku = inventorySupply.getSellerSKU();
            AmazonInventory amazonInventory = amazonInventoryRepository.findByAsinAndSellerSku(asin, sellerSku);
            if (amazonInventory != null) {
                LOGGER.info("updating inventory {}: {}", asin, sellerSku);
            } else {
                LOGGER.info("saving inventory {}: {}", asin, sellerSku);
                amazonInventory = new AmazonInventory();
                amazonInventory.setMarket(market);
                amazonInventory.setStore(store);
                amazonInventory.setAsin(asin);
                amazonInventory.setSellerSku(sellerSku);
            }
            amazonInventory.setFnSku(inventorySupply.getFNSKU());
            amazonInventory.setInStockQuantity(inventorySupply.getInStockSupplyQuantity());
            amazonInventory.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            amazonInventoryRepository.save(amazonInventory);
        }
    }

    /**
     * @return if order already existed, return false; else return true;
     */
    private boolean saveOrUpdate(String store, String market, Order order) {
        boolean alreadyExisted = false;
        AmazonOrder amazonOrder = amazonOrderRepository.findByAmazonOrderId(order.getAmazonOrderId());
        if (amazonOrder != null){
            LOGGER.info("updating order {}: {}", order.getAmazonOrderId(), order.getOrderStatus());
            alreadyExisted = true;
        } else {
            amazonOrder = new AmazonOrder(order);
            LOGGER.info("saving order {}: {}", order.getAmazonOrderId(), order.getOrderStatus());
        }
        amazonOrder.setStore(store);
        amazonOrder.setMarket(market);
        amazonOrderRepository.save(amazonOrder);
        return !alreadyExisted;
    }

    private void saveOrderItem(String store, String market, String amazonOrderId, OrderItem orderItem) {
        LOGGER.info("saving orderItem {} of {}", orderItem.getOrderItemId(), amazonOrderId);
        AmazonOrderItem amazonOrderItem = new AmazonOrderItem(amazonOrderId, orderItem);
        amazonOrderItem.setStore(store);
        amazonOrderItem.setMarket(market);
        amazonOrderItemRepository.save(amazonOrderItem);
    }
}
