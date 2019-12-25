package com.onlymaker.scorpio.data;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "amazon_age_report")
public class AmazonAge {
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

    @Column(name = "sku")
    private String sku;

    @Column(name = "size")
    private String size;

    @Column(name = "seller_sku")
    private String sellerSku;

    @Column(name = "currency")
    private String currency;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "age_90")
    private int age90;

    @Column(name = "age_180")
    private int age180;

    @Column(name = "age_270")
    private int age270;

    @Column(name = "age_365")
    private int age365;

    @Column(name = "age_year_plus")
    private int ageYearPlus;

    @Column(name = "date")
    private Date date;

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

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAge90() {
        return age90;
    }

    public void setAge90(int age90) {
        this.age90 = age90;
    }

    public int getAge180() {
        return age180;
    }

    public void setAge180(int age180) {
        this.age180 = age180;
    }

    public int getAge270() {
        return age270;
    }

    public void setAge270(int age270) {
        this.age270 = age270;
    }

    public int getAge365() {
        return age365;
    }

    public void setAge365(int age365) {
        this.age365 = age365;
    }

    public int getAgeYearPlus() {
        return ageYearPlus;
    }

    public void setAgeYearPlus(int ageYearPlus) {
        this.ageYearPlus = ageYearPlus;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
