package com.onlymaker.scorpio.data;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.InboundShipmentInfo;
import com.onlymaker.scorpio.mws.Utils;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "amazon_inbound")
public class AmazonInbound {
    public static final String STATUS_WORKING = "WORKING";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    public static final String STATUS_RECEIVING = "RECEIVING";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_DELETED = "DELETED";
    public static final String STATUS_ERROR = "ERROR";

    public AmazonInbound() {
    }

    public AmazonInbound(InboundShipmentInfo info) {
        shipmentId = info.getShipmentId();
        shipmentName = info.getShipmentName();
        status = info.getShipmentStatus();
        destCenterId = info.getDestinationFulfillmentCenterId();
        data = Utils.getJsonString(info);
        createTime = new Timestamp(System.currentTimeMillis());
    }

    public static final List<String> COUNTING_STATUS_LIST = new ArrayList<String>() {{
        add(STATUS_WORKING);
        add(STATUS_SHIPPED);
        add(STATUS_IN_TRANSIT);
        add(STATUS_DELIVERED);
        add(STATUS_CHECKED_IN);
    }};

    public static final List<String> STOP_COUNTING_STATUS_LIST = new ArrayList<String>() {{
        add(STATUS_RECEIVING);
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

    @Column(name = "dest_center_id")
    private String destCenterId;

    @Column(name = "data")
    private String data;

    @Column(name = "market")
    private String market;

    @Column(name = "dest")
    private String dest;

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

    public String getDestCenterId() {
        return destCenterId;
    }

    public void setDestCenterId(String destCenterId) {
        this.destCenterId = destCenterId;
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

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
