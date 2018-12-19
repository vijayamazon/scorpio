package com.onlymaker.scorpio.data;

import com.amazonservices.mws.orders._2013_09_01.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "amazon_order")
public class AmazonOrder {
    private static final String FBA = "AFN";
    private static final String MFN = "MFN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "amazon_order_id")
    private String amazonOrderId;

    @Column(name = "market")
    private String market;

    @Column(name = "status")
    private String status;

    @Column(name = "fulfillment")
    private Integer fulfillment;

    @Column(name = "data")
    private String data;

    @Column(name = "create_time")
    private Timestamp createTime;

    public AmazonOrder() {}

    public AmazonOrder(Order order) {
        amazonOrderId = order.getAmazonOrderId();
        status = order.getOrderStatus();
        fulfillment = Objects.equals(order.getFulfillmentChannel(), FBA) ? 0 : 1;
        try {
            data = new ObjectMapper().writeValueAsString(order);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            data = "";
        }
        createTime = new Timestamp(System.currentTimeMillis());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAmazonOrderId() {
        return amazonOrderId;
    }

    public void setAmazonOrderId(String amazonOrderId) {
        this.amazonOrderId = amazonOrderId;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getFulfillment() {
        return fulfillment;
    }

    public void setFulfillment(Integer fulfillment) {
        this.fulfillment = fulfillment;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
