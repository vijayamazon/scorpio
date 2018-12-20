package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.orders._2013_09_01.model.ListOrderItemsByNextTokenResult;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrderItemsResult;
import com.amazonservices.mws.orders._2013_09_01.model.Order;
import com.amazonservices.mws.orders._2013_09_01.model.OrderItem;
import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class OrderServiceTest {
    private OrderService orderService;
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;

    @Before
    public void setup() {
        orderService = new OrderService(appInfo, amazon.getList().get(0));
    }

    @Test
    public void listOrders() {
        orderService
                .getListOrdersResponseByCreateTimeLastDay()
                .getListOrdersResult()
                .getOrders()
                .forEach(this::print);
    }

    private void listOrderItems(String amazonOrderId) {
        ListOrderItemsResult result = orderService.getListOrderItemsResponse(amazonOrderId).getListOrderItemsResult();
        result.getOrderItems().forEach(this::print);
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            System.out.println("========== next token: " + nextToken);
            ListOrderItemsByNextTokenResult nextResult = orderService.getListOrderItemsByNextTokenResponse(nextToken).getListOrderItemsByNextTokenResult();
            nextResult.getOrderItems().forEach(this::print);
            nextToken = nextResult.getNextToken();
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void print(Object o) {
        if (o instanceof Order) {
            Order order = (Order) o;
            System.out.println("order:" + order.getAmazonOrderId());
            listOrderItems(order.getAmazonOrderId());
        } else if (o instanceof OrderItem) {
            OrderItem orderItem = (OrderItem) o;
            System.out.println("item:" + orderItem.getOrderItemId() + ":" + orderItem.getQuantityOrdered());
        }
    }
}
