package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSAsyncClient;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSClient;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.*;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;

import java.time.LocalDate;

import static com.amazonservices.mws.FulfillmentInventory._2010_10_01.samples.ListInventorySupplySample.invokeListInventorySupply;

public class InventoryService {
    private static final long FETCH_INVENTORY_INTERVAL_IN_MS = 5000;
    /** The client, lazy initialized. Async client is also a sync client. */
    private FBAInventoryServiceMWSAsyncClient client;
    private AppInfo appInfo;
    private MarketWebService mws;

    public InventoryService(AppInfo appInfo, MarketWebService mws) {
        this.appInfo = appInfo;
        this.mws = mws;
    }

    public MarketWebService getMws() {
        return mws;
    }

    public GetServiceStatusResponse getServiceStatusResponse() {
        GetServiceStatusRequest request = new GetServiceStatusRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        return getClient().getServiceStatus(request);
    }

    /**
     * @see <a href="https://docs.developer.amazonservices.com/en_US/fba_inventory/FBAInventory_ListInventorySupply.html">FBAInventory_ListInventorySupply.html</a>
     * maximum request quota: 30
     * restore rate: 2/s
     */
    public ListInventorySupplyResponse getListInventorySupplyResponse(ListInventorySupplyRequest request) {
        forceWaiting(FETCH_INVENTORY_INTERVAL_IN_MS);
        return invokeListInventorySupply(getClient(), request);
    }

    public ListInventorySupplyByNextTokenResponse getListInventorySupplyByNextTokenResponse(String nextToken) {
        forceWaiting(FETCH_INVENTORY_INTERVAL_IN_MS);
        ListInventorySupplyByNextTokenRequest request = new ListInventorySupplyByNextTokenRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setNextToken(nextToken);
        return getClient().listInventorySupplyByNextToken(request);
    }

    public ListInventorySupplyResponse getListInventorySupplyResponseWithSku(SellerSkuList sellerSkuList) {
        ListInventorySupplyRequest request = new ListInventorySupplyRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setSellerSkus(sellerSkuList);
        return getListInventorySupplyResponse(request);
    }

    public ListInventorySupplyRequest buildRequestWithinLastDay() {
        ListInventorySupplyRequest request = new ListInventorySupplyRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        LocalDate date = LocalDate.now();
        request.setQueryStartDateTime(Utils.getXMLGregorianCalendar(date.minusDays(1)));
        return request;
    }

    /**
     * Get a client connection ready to use.
     *
     * @return A ready to use client connection.
     */
    private FBAInventoryServiceMWSClient getClient() {
        return getAsyncClient();
    }

    /**
     * Get an async client connection ready to use.
     *
     * @return A ready to use client connection.
     */
    private synchronized FBAInventoryServiceMWSAsyncClient getAsyncClient() {
        if (client == null) {
            FBAInventoryServiceMWSConfig config = new FBAInventoryServiceMWSConfig();
            config.setServiceURL(mws.getMarketplaceUrl());
            client = new FBAInventoryServiceMWSAsyncClient(
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
