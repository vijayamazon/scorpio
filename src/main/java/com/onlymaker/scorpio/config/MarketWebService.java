package com.onlymaker.scorpio.config;

public class MarketWebService {
    private String marketplace;
    private String marketplaceId;
    private String marketplaceUrl;
    private String sellerId;
    private String accessKey;
    private String secretKey;
    private String authToken;

    public String getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(String marketplace) {
        this.marketplace = marketplace;
    }

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public String getMarketplaceUrl() {
        return marketplaceUrl;
    }

    public void setMarketplaceUrl(String marketplaceUrl) {
        this.marketplaceUrl = marketplaceUrl;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
