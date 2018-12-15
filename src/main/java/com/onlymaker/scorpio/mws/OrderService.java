package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersAsyncClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders._2013_09_01.model.*;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Collections;

public class OrderService {
    private static MarketplaceWebServiceOrdersAsyncClient client;
    private MarketWebService mws;
    @Autowired
    AppInfo appInfo;

    public OrderService(MarketWebService mws) {
        this.mws = mws;
    }

    public MarketWebService getMws() {
        return mws;
    }

    public ListOrdersResponse getListOrdersResponseByCreateTimeLastDay() {
        ListOrdersRequest request = new ListOrdersRequest();
        LocalDate date = LocalDate.now();
        request.setCreatedAfter(Utils.getXMLGregorianCalendar(date.minusDays(1)));
        return getListOrdersResponse(request);
    }

    public ListOrdersResponse getListOrdersResponseByUpdateTimeLast30Days() {
        ListOrdersRequest request = new ListOrdersRequest();
        LocalDate date = LocalDate.now();
        request.setLastUpdatedAfter(Utils.getXMLGregorianCalendar(date.minusDays(30)));
        return getListOrdersResponse(request);
    }

    public ListOrdersByNextTokenResponse getListOrdersByNextTokenResponse(String nextToken) {
        ListOrdersByNextTokenRequest request = new ListOrdersByNextTokenRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setNextToken(nextToken);
        return getClient().listOrdersByNextToken(request);
    }

    public ListOrderItemsResponse getListOrderItemsResponse(String orderId) {
        ListOrderItemsRequest request = new ListOrderItemsRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setAmazonOrderId(orderId);
        return getClient().listOrderItems(request);
    }

    public ListOrderItemsByNextTokenResponse getListOrderItemsByNextTokenResponse(String nextToken) {
        ListOrderItemsByNextTokenRequest request = new ListOrderItemsByNextTokenRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setNextToken(nextToken);
        return getClient().listOrderItemsByNextToken(request);
    }

    private ListOrdersResponse getListOrdersResponse(ListOrdersRequest request) {
        request.setSellerId(mws.getSellerId());
        request.setMarketplaceId(Collections.singletonList(mws.getMarketplaceId()));
        return getClient().listOrders(request);
    }

    private synchronized MarketplaceWebServiceOrdersAsyncClient getClient() {
        if (client == null) {
            MarketplaceWebServiceOrdersConfig config = new MarketplaceWebServiceOrdersConfig();
            config.setServiceURL(mws.getMarketplaceUrl());
            client = new MarketplaceWebServiceOrdersAsyncClient(
                    mws.getAccessKey(),
                    mws.getSecretKey(),
                    appInfo.getName(),
                    appInfo.getVersion(),
                    config, null);
        }
        return client;
    }
}
