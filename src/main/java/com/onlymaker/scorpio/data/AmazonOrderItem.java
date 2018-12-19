package com.onlymaker.scorpio.data;

import com.amazonservices.mws.orders._2013_09_01.model.OrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "amazon_order_item")
public class AmazonOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "amazon_order_id")
    private String amazonOrderId;

    @Column(name = "amazon_order_item_id")
    private String amazonOrderItemId;

    @Column(name = "market")
    private String market;

    @Column(name = "asin")
    private String asin;

    @Column(name = "seller_sku")
    private String sellerSku;

    @Column(name = "data")
    private String data;

    @Column(name = "create_time")
    private Timestamp createTime;

    public AmazonOrderItem() {}

    public AmazonOrderItem(String id, OrderItem orderItem) {
        amazonOrderId = id;
        amazonOrderItemId = orderItem.getOrderItemId();
        asin = orderItem.getASIN();
        sellerSku = orderItem.getSellerSKU();
        try {
            data = new ObjectMapper().writeValueAsString(orderItem);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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

    public String getAmazonOrderItemId() {
        return amazonOrderItemId;
    }

    public void setAmazonOrderItemId(String amazonOrderItemId) {
        this.amazonOrderItemId = amazonOrderItemId;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
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
