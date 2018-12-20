package com.onlymaker.scorpio.data;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "amazon_inventory")
public class AmazonInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "market")
    private String market;

    @Column(name = "asin")
    private String asin;

    @Column(name = "fn_sku")
    private String fnSku;

    @Column(name = "seller_sku")
    private String sellerSku;

    @Column(name = "in_stock_quantity")
    private Integer inStockQuantity;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "fulfillment")
    private String fulfillment;

    @Column(name = "create_date")
    private Date createDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFnSku() {
        return fnSku;
    }

    public void setFnSku(String fnSku) {
        this.fnSku = fnSku;
    }

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
    }

    public Integer getInStockQuantity() {
        return inStockQuantity;
    }

    public void setInStockQuantity(Integer inStockQuantity) {
        this.inStockQuantity = inStockQuantity;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getFulfillment() {
        return fulfillment;
    }

    public void setFulfillment(String fulfillment) {
        this.fulfillment = fulfillment;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}