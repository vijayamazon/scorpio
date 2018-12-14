package com.onlymaker.scorpio.task;

import com.amazonservices.mws.orders._2013_09_01.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlymaker.scorpio.data.AmazonOrder;
import com.onlymaker.scorpio.data.AmazonOrderItem;
import com.onlymaker.scorpio.data.AmazonOrderItemRepository;
import com.onlymaker.scorpio.data.AmazonOrderRepository;
import com.onlymaker.scorpio.mws.Configuration;
import com.onlymaker.scorpio.mws.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@ConditionalOnProperty(prefix = "mws", name = "mode", havingValue = "satellite")
public class AmazonOrderFetch {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonOrderFetch.class);
    private static final int LIST_ORDER_INTERVAL_IN_MINUTES = 1;
    private static final int LIST_ORDER_ITEM_INTERVAL_IN_SECONDS = 10;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    Configuration configuration;
    @Autowired
    OrderService orderService;
    @Autowired
    AmazonOrderRepository amazonOrderRepository;
    @Autowired
    AmazonOrderItemRepository amazonOrderItemRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void everyday() {
        LOGGER.info("everyday fetch task ...");
        handling(orderService.getListOrdersResponseByCreateTimeLastDay(), this::saveOrder);
        handling(orderService.getListOrdersResponseByUpdateTimeLast30Days(), this::updateOrder);
    }

    private void handling(ListOrdersResponse response, Consumer<Order> consumer) {
        ListOrdersResult result = response.getListOrdersResult();
        result.getOrders().forEach(consumer);
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            try {
                TimeUnit.MINUTES.sleep(LIST_ORDER_INTERVAL_IN_MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ListOrdersByNextTokenResult nextResult = orderService.getListOrdersByNextTokenResponse(nextToken).getListOrdersByNextTokenResult();
            nextResult.getOrders().forEach(consumer);
            nextToken = nextResult.getNextToken();
        }
    }

    private void saveOrder(Order order) {
        LOGGER.info("saving order {}: {}", order.getAmazonOrderId(), order.getOrderStatus());
        save(order);
        String amazonOrderId = order.getAmazonOrderId();
        ListOrderItemsResult result = orderService.getListOrderItemsResponse(amazonOrderId).getListOrderItemsResult();
        result.getOrderItems().forEach(o -> save(amazonOrderId, o));
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            try {
                TimeUnit.SECONDS.sleep(LIST_ORDER_ITEM_INTERVAL_IN_SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ListOrderItemsByNextTokenResult nextResult = orderService.getListOrderItemsByNextTokenResponse(nextToken).getListOrderItemsByNextTokenResult();
            nextResult.getOrderItems().forEach(o -> save(amazonOrderId, o));
            nextToken = nextResult.getNextToken();
        }
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

    private void save(Order order) {
        try {
            AmazonOrder amazonOrder = new AmazonOrder(order);
            amazonOrder.setMarket(configuration.getMarketplace());
            amazonOrder.setStore(configuration.getAppName());
            amazonOrderRepository.save(amazonOrder);
        } catch (Throwable t) {
            LOGGER.error("saving order error: {}", order.getAmazonOrderId(), t);
        }
    }

    private void save(String amazonOrderId, OrderItem orderItem) {
        try {
            LOGGER.info("saving order {} item {}", amazonOrderId, orderItem.getOrderItemId());
            AmazonOrderItem amazonOrderItem = new AmazonOrderItem(amazonOrderId, orderItem);
            amazonOrderItem.setMarket(configuration.getMarketplace());
            amazonOrderItem.setStore(configuration.getAppName());
            amazonOrderItemRepository.save(amazonOrderItem);
        } catch (Throwable t) {
            LOGGER.error("saving order item error: {}", orderItem.getOrderItemId(), t);
        }
    }
}
