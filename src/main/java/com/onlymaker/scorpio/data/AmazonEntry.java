package com.onlymaker.scorpio.data;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "amazon_entry_2")
public class AmazonEntry {
    public static final int STATUS_INVALID = -1;
    public static final int STATUS_WATCHING = 0;
    public static final int STATUS_RAMP_UP = 1;
    public static final int STATUS_KEEP = 2;
    public static final int STATUS_DISCARD = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "market")
    private String market;

    @Column(name = "asin")
    private String asin;

    @Column(name = "status")
    private Integer status;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
