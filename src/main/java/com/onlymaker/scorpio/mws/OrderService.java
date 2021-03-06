package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersAsyncClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders._2013_09_01.model.*;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderService {
    private static final long FETCH_ORDER_INTERVAL_IN_MS = 60000;
    private static final long FETCH_ORDER_ITEM_INTERVAL_IN_MS = 5000;
    private static final List<String> STATUS_FILTER = new ArrayList<String>() {{
        add("Unshipped");
        add("PartiallyShipped");
        add("Shipped");
    }};
    private MarketplaceWebServiceOrdersAsyncClient client;
    private AppInfo appInfo;
    private MarketWebService mws;

    public OrderService(AppInfo appInfo, MarketWebService mws) {
        this.appInfo = appInfo;
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

    public ListOrdersResponse getListOrdersResponseByUpdateTimeWithinDays(long days) {
        ListOrdersRequest request = new ListOrdersRequest();
        LocalDate date = LocalDate.now();
        request.setLastUpdatedAfter(Utils.getXMLGregorianCalendar(date.minusDays(days)));
        request.setLastUpdatedBefore(Utils.getXMLGregorianCalendar(date.minusDays(1)));
        return getListOrdersResponse(request);
    }

    /**
     * @see <a href="https://docs.developer.amazonservices.com/en_US/orders-2013-09-01/Orders_ListOrders.html">Orders_ListOrders.html</a>
     * maximum request quota: 6
     * restore rate: 1/min
     */
    private ListOrdersResponse getListOrdersResponse(ListOrdersRequest request) {
        forceWaiting(FETCH_ORDER_INTERVAL_IN_MS);
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setMarketplaceId(Collections.singletonList(mws.getMarketplaceId()));
        request.setOrderStatus(STATUS_FILTER);
        return getClient().listOrders(request);
    }

    public ListOrdersByNextTokenResponse getListOrdersByNextTokenResponse(String nextToken) {
        forceWaiting(FETCH_ORDER_INTERVAL_IN_MS);
        ListOrdersByNextTokenRequest request = new ListOrdersByNextTokenRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setNextToken(nextToken);
        return getClient().listOrdersByNextToken(request);
    }

    /**
     * @see <a href="https://docs.developer.amazonservices.com/en_US/orders-2013-09-01/Orders_ListOrderItems.html">Orders_ListOrderItems.html</a>
     * maximum request quota: 30
     * restore rate: 1/2s
     */
    public ListOrderItemsResponse getListOrderItemsResponse(String orderId) {
        forceWaiting(FETCH_ORDER_ITEM_INTERVAL_IN_MS);
        ListOrderItemsRequest request = new ListOrderItemsRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setAmazonOrderId(orderId);
        return getClient().listOrderItems(request);
    }

    public ListOrderItemsByNextTokenResponse getListOrderItemsByNextTokenResponse(String nextToken) {
        forceWaiting(FETCH_ORDER_ITEM_INTERVAL_IN_MS);
        ListOrderItemsByNextTokenRequest request = new ListOrderItemsByNextTokenRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setNextToken(nextToken);
        return getClient().listOrderItemsByNextToken(request);
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

    private void forceWaiting(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
