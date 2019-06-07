package com.onlymaker.scorpio.data;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.InboundShipmentItem;
import com.onlymaker.scorpio.mws.Utils;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "amazon_inbound_item")
public class AmazonInboundItem {
    public AmazonInboundItem() {
    }

    public AmazonInboundItem(AmazonInbound inbound, InboundShipmentItem item) {
        shipmentId = inbound.getShipmentId();
        shipmentName = inbound.getShipmentName();
        status = inbound.getStatus();
        market = inbound.getMarket();
        sellerSku = item.getSellerSKU();
        fnSku = item.getFulfillmentNetworkSKU();
        quantityShipped = item.getQuantityShipped();
        quantityReceived = item.getQuantityReceived();
        data = Utils.getJsonString(item);
        createTime = new Timestamp(System.currentTimeMillis());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "shipment_id")
    private String shipmentId;

    @Column(name = "shipment_name")
    private String shipmentName;

    @Column(name = "status")
    private String status;

    @Column(name = "data")
    private String data;

    @Column(name = "market")
    private String market;

    @Column(name = "seller_sku")
    private String sellerSku;

    @Column(name = "fn_sku")
    private String fnSku;

    @Column(name = "sku")
    private String sku;

    @Column(name = "size")
    private String size;

    @Column(name = "quantity_shipped")
    private Integer quantityShipped;

    @Column(name = "quantity_received")
    private Integer quantityReceived;

    @Column(name = "create_time")
    private Timestamp createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getShipmentName() {
        return shipmentName;
    }

    public void setShipmentName(String shipmentName) {
        this.shipmentName = shipmentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
    }

    public String getFnSku() {
        return fnSku;
    }

    public void setFnSku(String fnSku) {
        this.fnSku = fnSku;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getQuantityShipped() {
        return quantityShipped;
    }

    public void setQuantityShipped(Integer quantityShipped) {
        this.quantityShipped = quantityShipped;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
