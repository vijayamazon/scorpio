package com.onlymaker.scorpio.task;

import com.amazonservices.mws.orders._2013_09_01.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;
import com.onlymaker.scorpio.data.AmazonOrder;
import com.onlymaker.scorpio.data.AmazonOrderItem;
import com.onlymaker.scorpio.data.AmazonOrderItemRepository;
import com.onlymaker.scorpio.data.AmazonOrderRepository;
import com.onlymaker.scorpio.mws.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(prefix = "app", name = "mode", havingValue = "satellite")
public class AmazonOrderFetch {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonOrderFetch.class);
    private static final long HOUR_IN_MS = 3600000;
    private static final long INIT_DELAY = 2 * HOUR_IN_MS;
    private static final long FIX_DELAY = 24 * HOUR_IN_MS;
    private static final int LIST_ORDER_INTERVAL_IN_MINUTES = 1;
    private static final int LIST_ORDER_ITEM_INTERVAL_IN_SECONDS = 10;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;
    @Autowired
    AmazonOrderRepository amazonOrderRepository;
    @Autowired
    AmazonOrderItemRepository amazonOrderItemRepository;

    @Scheduled(initialDelay = INIT_DELAY, fixedDelay = FIX_DELAY)
    public void everyday() {
        LOGGER.info("everyday fetch task ...");
        try {
            OrderService orderService;
            List<MarketWebService> list = amazon.getList();
            for (MarketWebService mws : list) {
                orderService = new OrderService(mws);
                fetchOrder(orderService);
                updateOrder(orderService);
            }
        } catch (Throwable t) {
            LOGGER.info("fetch order unexpected error: {}", t.getMessage(), t);
        }
    }

    private void fetchOrder(OrderService orderService) {
        ListOrdersResult result = orderService.getListOrdersResponseByCreateTimeLastDay().getListOrdersResult();
        List<Order> list = result.getOrders();
        for (Order order : list) {
            saveOrder(orderService.getMws().getStore(), orderService.getMws().getMarketplace(), order);
            fetchOrderItem(orderService, order.getAmazonOrderId());
        }
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            try {
                TimeUnit.MINUTES.sleep(LIST_ORDER_INTERVAL_IN_MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ListOrdersByNextTokenResult nextResult = orderService.getListOrdersByNextTokenResponse(nextToken).getListOrdersByNextTokenResult();
            list = nextResult.getOrders();
            for (Order order : list) {
                saveOrder(orderService.getMws().getStore(), orderService.getMws().getMarketplace(), order);
                fetchOrderItem(orderService, order.getAmazonOrderId());
            }
            nextToken = nextResult.getNextToken();
        }
    }

    private void fetchOrderItem(OrderService orderService, String amazonOrderId) {
        ListOrderItemsResult result = orderService.getListOrderItemsResponse(amazonOrderId).getListOrderItemsResult();
        result.getOrderItems().forEach(o -> saveOrderItem(orderService.getMws().getStore(), orderService.getMws(). getMarketplace(), amazonOrderId, o));
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            try {
                TimeUnit.SECONDS.sleep(LIST_ORDER_ITEM_INTERVAL_IN_SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ListOrderItemsByNextTokenResult nextResult = orderService.getListOrderItemsByNextTokenResponse(nextToken).getListOrderItemsByNextTokenResult();
            nextResult.getOrderItems().forEach(o -> saveOrderItem(orderService.getMws().getStore(), orderService.getMws().getMarketplace(), amazonOrderId, o));
            nextToken = nextResult.getNextToken();
        }
    }

    private void updateOrder(OrderService orderService) {
        ListOrdersResult result = orderService.getListOrdersResponseByUpdateTimeLast30Days().getListOrdersResult();
        result.getOrders().forEach(this::updateOrder);
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            try {
                TimeUnit.MINUTES.sleep(LIST_ORDER_INTERVAL_IN_MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ListOrdersByNextTokenResult nextResult = orderService.getListOrdersByNextTokenResponse(nextToken).getListOrdersByNextTokenResult();
            nextResult.getOrders().forEach(this::updateOrder);
            nextToken = nextResult.getNextToken();
        }
    }

    private void saveOrder(String store, String market, Order order) {
        LOGGER.info("saving order {}: {}", order.getAmazonOrderId(), order.getOrderStatus());
        AmazonOrder amazonOrder = new AmazonOrder(order);
        amazonOrder.setStore(store);
        amazonOrder.setMarket(market);
        amazonOrderRepository.save(amazonOrder);
    }

    private void saveOrderItem(String store, String market, String amazonOrderId, OrderItem orderItem) {
        LOGGER.info("saving orderItem {} of {}", orderItem.getOrderItemId(), amazonOrderId);
        AmazonOrderItem amazonOrderItem = new AmazonOrderItem(amazonOrderId, orderItem);
        amazonOrderItem.setStore(store);
        amazonOrderItem.setMarket(market);
        amazonOrderItemRepository.save(amazonOrderItem);
    }

    private void updateOrder(Order order) {
        AmazonOrder amazonOrder = amazonOrderRepository.findByAmazonOrderId(order.getAmazonOrderId());
        if (amazonOrder != null) {
            String status = order.getOrderStatus();
            LOGGER.info("updating order {}: {}", order.getAmazonOrderId(), status);
            amazonOrder.setStatus(status);
            try {
                amazonOrder.setData(MAPPER.writeValueAsString(order));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            amazonOrderRepository.save(amazonOrder);
        }
    }
}
