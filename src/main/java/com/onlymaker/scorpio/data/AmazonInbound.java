package com.onlymaker.scorpio.data;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.InboundShipmentInfo;
import com.onlymaker.scorpio.mws.Utils;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "amazon_inbound")
public class AmazonInbound {
    public static final String STATUS_WORKING = "WORKING";//The shipment was created by the seller, but has not yet shipped.
    public static final String STATUS_SHIPPED = "SHIPPED";//The shipment was picked up by the carrier.
    public static final String STATUS_IN_TRANSIT = "IN_TRANSIT";//The carrier has notified the Amazon fulfillment center that it is aware of the shipment.
    public static final String STATUS_DELIVERED = "DELIVERED";//The shipment was delivered by the carrier to the Amazon fulfillment center.
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";//The shipment was checked-in at the receiving dock of the Amazon fulfillment center.
    public static final String STATUS_RECEIVING = "RECEIVING";//The shipment has arrived at the Amazon fulfillment center, but not all items have been marked as received.
    public static final String STATUS_CLOSED = "CLOSED";//The shipment has arrived at the Amazon fulfillment center and all items have been marked as received.
    public static final String STATUS_CANCELLED = "CANCELLED";//The shipment was cancelled by the seller after the shipment was sent to Amazon's fulfillment network.
    public static final String STATUS_DELETED = "DELETED";//The shipment was cancelled by the seller before the shipment was sent to the Amazon fulfillment center.
    public static final String STATUS_ERROR = "ERROR";//There was an error with the shipment and it was not processed by Amazon.

    public AmazonInbound() {
    }

    public AmazonInbound(InboundShipmentInfo info) {
        shipmentId = info.getShipmentId();
        shipmentName = info.getShipmentName();
        status = info.getShipmentStatus();
        data = Utils.getJsonString(info);
        createTime = new Timestamp(System.currentTimeMillis());
    }

    public static final List<String> IN_PROGRESS_STATUS_LIST = new ArrayList<String>() {{
        add(STATUS_WORKING);
        add(STATUS_SHIPPED);
        add(STATUS_IN_TRANSIT);
        add(STATUS_DELIVERED);
        add(STATUS_CHECKED_IN);
        add(STATUS_RECEIVING);
    }};

    public static final List<String> FINAL_STATUS_LIST = new ArrayList<String>() {{
        add(STATUS_CLOSED);
        add(STATUS_CANCELLED);
        add(STATUS_DELETED);
        add(STATUS_ERROR);
    }};

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

    @Column(name = "create_time")
    private Timestamp createTime;

    @Column(name = "receive_date")
    private Date receiveDate;

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

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Date getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(Date receiveDate) {
        this.receiveDate = receiveDate;
    }
}
