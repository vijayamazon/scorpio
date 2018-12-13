package com.onlymaker.scorpio.api;

import com.amazonservices.mws.orders._2013_09_01.model.Order;
import com.onlymaker.scorpio.mws.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Amazon {
    @Autowired
    OrderService orderService;

    @RequestMapping("/validate/order")
    private Order validateOrder() {
        return orderService.getListOrdersResponseByCreateTimeLastDay().getListOrdersResult().getOrders().get(0);
    }
}
