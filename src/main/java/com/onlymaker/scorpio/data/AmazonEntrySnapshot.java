package com.onlymaker.scorpio.data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "amazon_entry_snapshot")
public class AmazonEntrySnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "market")
    private String market;

    @Column(name = "store")
    private String store;

    @Column(name = "asin")
    private String asin;

    @Column(name = "sku")
    private String sku;

    @Column(name = "variable")
    private Integer variable;

    @Column(name = "rank_best")
    private Integer rankBest;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "star_average")
    private Float starAverage;

    @Column(name = "star_1")
    private Integer star1;

    @Column(name = "star_2")
    private Integer star2;

    @Column(name = "star_3")
    private Integer star3;

    @Column(name = "star_4")
    private Integer star4;

    @Column(name = "star_5")
    private Integer star5;

    @Column(name = "create_time")
    private Timestamp createTime;

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

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getVariable() {
        return variable;
    }

    public void setVariable(Integer variable) {
        this.variable = variable;
    }

    public Integer getRankBest() {
        return rankBest;
    }

    public void setRankBest(Integer rankBest) {
        this.rankBest = rankBest;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Float getStarAverage() {
        return starAverage;
    }

    public void setStarAverage(Float starAverage) {
        this.starAverage = starAverage;
    }

    public Integer getStar1() {
        return star1;
    }

    public void setStar1(Integer star1) {
        this.star1 = star1;
    }

    public Integer getStar2() {
        return star2;
    }

    public void setStar2(Integer star2) {
        this.star2 = star2;
    }

    public Integer getStar3() {
        return star3;
    }

    public void setStar3(Integer star3) {
        this.star3 = star3;
    }

    public Integer getStar4() {
        return star4;
    }

    public void setStar4(Integer star4) {
        this.star4 = star4;
    }

    public Integer getStar5() {
        return star5;
    }

    public void setStar5(Integer star5) {
        this.star5 = star5;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
