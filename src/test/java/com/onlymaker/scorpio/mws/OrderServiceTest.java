package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.orders._2013_09_01.model.Order;
import com.onlymaker.scorpio.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class OrderServiceTest {
    @Autowired
    OrderService orderService;

    @Test
    public void listOrders() {
        orderService.getListOrdersResponse().getListOrdersResult().getOrders().forEach(order -> printOrder(order));
    }

    private void printOrder(Order order) {
        System.out.println("==========" + order.getAmazonOrderId() + "==========");
        System.out.println(order.getBuyerEmail());
        System.out.println(order.getOrderStatus());
        System.out.println(order.getOrderTotal().getCurrencyCode() + ":" + order.getOrderTotal().getAmount());
    }
}
