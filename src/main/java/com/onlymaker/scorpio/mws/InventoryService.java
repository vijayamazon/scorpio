package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSAsyncClient;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSClient;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.*;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;

import java.time.LocalDate;

import static com.amazonservices.mws.FulfillmentInventory._2010_10_01.samples.ListInventorySupplySample.invokeListInventorySupply;

/**
 * @see <a href="https://docs.developer.amazonservices.com/en_US/fba_inventory/FBAInventory_ListInventorySupply.html">FBAInventory_ListInventorySupply.html</a>
 * maximum request quota of 30
 * two requests every second
 */
public class InventoryService {
    /** The client, lazy initialized. Async client is also a sync client. */
    private static FBAInventoryServiceMWSAsyncClient client;
    private AppInfo appInfo;
    private MarketWebService mws;

    public InventoryService(AppInfo appInfo, MarketWebService mws) {
        this.appInfo = appInfo;
        this.mws = mws;
    }

    public GetServiceStatusResponse getServiceStatusResponse() {
        GetServiceStatusRequest request = new GetServiceStatusRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        return getClient().getServiceStatus(request);
    }

    public ListInventorySupplyResponse getListInventorySupplyResponse(ListInventorySupplyRequest request) {
        return invokeListInventorySupply(getClient(), request);
    }

    public ListInventorySupplyByNextTokenResponse getListInventorySupplyByNextTokenResponse(String nextToken) {
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

    public ListInventorySupplyRequest buildRequestWithDate() {
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
        if (client==null) {
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
}
