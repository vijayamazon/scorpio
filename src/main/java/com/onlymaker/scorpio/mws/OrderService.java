package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersAsyncClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders._2013_09_01.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;

@Service
public class OrderService {
    private static MarketplaceWebServiceOrdersAsyncClient client;
    @Autowired
    Configuration configuration;

    public ListOrdersResponse getListOrdersResponseByCreateTimeLastDay() {
        ListOrdersRequest request = new ListOrdersRequest();
        LocalDate date = LocalDate.now();
        request.setCreatedAfter(Utils.getXMLGregorianCalendar(date.minusDays(1)));
        request.setCreatedBefore(Utils.getXMLGregorianCalendar(date));
        return getListOrdersResponse(request);
    }

    public ListOrdersResponse getListOrdersResponseByUpdateTimeLast30Days() {
        ListOrdersRequest request = new ListOrdersRequest();
        LocalDate date = LocalDate.now();
        request.setLastUpdatedAfter(Utils.getXMLGregorianCalendar(date.minusDays(30)));
        request.setLastUpdatedBefore(Utils.getXMLGregorianCalendar(date));
        return getListOrdersResponse(request);
    }

    public ListOrdersByNextTokenResponse getListOrdersByNextTokenResponse(String nextToken) {
        ListOrdersByNextTokenRequest request = new ListOrdersByNextTokenRequest();
        request.setSellerId(configuration.getSellerId());
        request.setMWSAuthToken(configuration.getAuthToken());
        request.setNextToken(nextToken);
        return getClient().listOrdersByNextToken(request);
    }

    public ListOrderItemsResponse getListOrderItemsResponse(String orderId) {
        ListOrderItemsRequest request = new ListOrderItemsRequest();
        request.setSellerId(configuration.getSellerId());
        request.setMWSAuthToken(configuration.getAuthToken());
        request.setAmazonOrderId(orderId);
        return getClient().listOrderItems(request);
    }

    public ListOrderItemsByNextTokenResponse getListOrderItemsByNextTokenResponse(String nextToken) {
        ListOrderItemsByNextTokenRequest request = new ListOrderItemsByNextTokenRequest();
        request.setSellerId(configuration.getSellerId());
        request.setMWSAuthToken(configuration.getAuthToken());
        request.setNextToken(nextToken);
        return getClient().listOrderItemsByNextToken(request);
    }

    private ListOrdersResponse getListOrdersResponse(ListOrdersRequest request) {
        request.setSellerId(configuration.getSellerId());
        request.setMarketplaceId(Collections.singletonList(configuration.getMarketplaceId()));
        return getClient().listOrders(request);
    }

    private synchronized MarketplaceWebServiceOrdersAsyncClient getClient() {
        if (client == null) {
            MarketplaceWebServiceOrdersConfig config = new MarketplaceWebServiceOrdersConfig();
            config.setServiceURL(configuration.getMarketplaceUrl());
            client = new MarketplaceWebServiceOrdersAsyncClient(
                    configuration.getAccessKey(),
                    configuration.getSecretKey(),
                    configuration.getAppName(),
                    configuration.getAppVersion(),
                    config, null);
        }
        return client;
    }
}
