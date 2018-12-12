package com.onlymaker.scorpio.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlymaker.scorpio.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class AmazonOrderTest {
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    AmazonOrderRepository amazonOrderRepository;
    @Autowired
    AmazonOrderItemRepository amazonOrderItemRepository;

    @Test
    public void read() {
        amazonOrderRepository.findAll().forEach(amazonOrder -> {
            String amazonOrderId = amazonOrder.getAmazonOrderId();
            System.out.println(amazonOrderId);
            amazonOrderItemRepository.findAllByAmazonOrderId(amazonOrderId).forEach(amazonOrderItem -> {
                try {
                    System.out.println(mapper.writeValueAsString(amazonOrderItem));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
