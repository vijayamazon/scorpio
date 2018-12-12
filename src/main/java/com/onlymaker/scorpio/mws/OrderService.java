package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.client.MwsUtl;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersAsyncClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders._2013_09_01.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

@Service
public class OrderService {
    private static MarketplaceWebServiceOrdersAsyncClient client;
    @Autowired
    Configuration configuration;

    public ListOrdersResponse getListOrdersResponse() {
        ListOrdersRequest request = new ListOrdersRequest();
        request.setSellerId(configuration.getSellerId());
        request.setMarketplaceId(Collections.singletonList(configuration.getMarketplaceId()));
        LocalDate date = LocalDate.now();
        request.setCreatedAfter(getXMLGregorianCalendar(date.minusDays(2)));
        request.setCreatedBefore(getXMLGregorianCalendar(date.minusDays(1)));
        return getClient().listOrders(request);
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

    private XMLGregorianCalendar getXMLGregorianCalendar(LocalDate date) {
        XMLGregorianCalendar result = MwsUtl.getDTF().newXMLGregorianCalendar();
        result.setYear(date.getYear());
        result.setMonth(date.getMonthValue());
        result.setDay(date.getDayOfMonth());
        result.setHour(0);
        result.setMinute(0);
        result.setSecond(0);
        return result;
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
