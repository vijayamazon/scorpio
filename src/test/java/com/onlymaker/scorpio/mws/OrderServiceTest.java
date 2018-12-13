package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.orders._2013_09_01.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.data.AmazonOrder;
import com.onlymaker.scorpio.data.AmazonOrderItem;
import com.onlymaker.scorpio.data.AmazonOrderItemRepository;
import com.onlymaker.scorpio.data.AmazonOrderRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class OrderServiceTest {
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    OrderService orderService;
    @Autowired
    AmazonOrderRepository amazonOrderRepository;
    @Autowired
    AmazonOrderItemRepository amazonOrderItemRepository;

    @Test
    public void listOrders() {
        ListOrdersResult result = orderService.getListOrdersResponseByCreateTimeLastDay().getListOrdersResult();
        result.getOrders().forEach(this::handle);
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            System.out.println("========== next token: " + nextToken);
            ListOrdersByNextTokenResult nextResult = orderService.getListOrdersByNextTokenResponse(nextToken).getListOrdersByNextTokenResult();
            nextResult.getOrders().forEach(this::handle);
            nextToken = nextResult.getNextToken();
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void listOrderItems(String amazonOrderId) {
        ListOrderItemsResult result = orderService.getListOrderItemsResponse(amazonOrderId).getListOrderItemsResult();
        result.getOrderItems().forEach(o -> handle(o, amazonOrderId));
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            System.out.println("========== next token: " + nextToken);
            ListOrderItemsByNextTokenResult nextResult = orderService.getListOrderItemsByNextTokenResponse(nextToken).getListOrderItemsByNextTokenResult();
            nextResult.getOrderItems().forEach(o -> handle(o, amazonOrderId));
            nextToken = nextResult.getNextToken();
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handle(Object o, String ... id) {
        try {
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
            if (o instanceof Order) {
                Order order = (Order) o;
                AmazonOrder amazonOrder = amazonOrderRepository.findByAmazonOrderId(order.getAmazonOrderId());
                if (amazonOrder == null) {
                    amazonOrder = new AmazonOrder(order);
                }
                amazonOrder.setMarket("market");
                amazonOrder.setStore("store");
                amazonOrderRepository.save(amazonOrder);
                listOrderItems(order.getAmazonOrderId());
            } else if (o instanceof OrderItem) {
                OrderItem orderItem = (OrderItem) o;
                AmazonOrderItem amazonOrderItem = amazonOrderItemRepository.findByAmazonOrderItemId(orderItem.getOrderItemId());
                if (amazonOrderItem == null) {
                    amazonOrderItem = new AmazonOrderItem(id[0], orderItem);
                }
                amazonOrderItem.setMarket("market");
                amazonOrderItem.setStore("store");
                amazonOrderItemRepository.save(amazonOrderItem);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
