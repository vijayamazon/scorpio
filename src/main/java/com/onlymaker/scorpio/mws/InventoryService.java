package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSAsyncClient;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSClient;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.*;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;
import org.springframework.beans.factory.annotation.Autowired;

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
    private MarketWebService mws;
    @Autowired
    AppInfo appInfo;

    public InventoryService(MarketWebService mws) {
        this.mws = mws;
    }

    public GetServiceStatusResponse getServiceStatusResponse() {
        GetServiceStatusRequest request = new GetServiceStatusRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setMarketplace(mws.getMarketplace());
        return getClient().getServiceStatus(request);
    }

    public ListInventorySupplyResponse getListInventorySupplyResponse(ListInventorySupplyRequest request) {
        return invokeListInventorySupply(getClient(), request);
    }

    public ListInventorySupplyByNextTokenResponse getListInventorySupplyByNextTokenResponse(String nextToken) {
        ListInventorySupplyByNextTokenRequest request = new ListInventorySupplyByNextTokenRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setMarketplace(mws.getMarketplace());
        request.setNextToken(nextToken);
        return getClient().listInventorySupplyByNextToken(request);
    }

    public ListInventorySupplyRequest buildRequestWithSku(String ... sku) {
        ListInventorySupplyRequest request = new ListInventorySupplyRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setMarketplace(mws.getMarketplace());
        request.setMarketplaceId(mws.getMarketplaceId());
        SellerSkuList sellerSkus = new SellerSkuList();
        sellerSkus.withMember(sku);
        request.setSellerSkus(sellerSkus);
        return request;
    }

    public ListInventorySupplyRequest buildRequestWithDate() {
        ListInventorySupplyRequest request = new ListInventorySupplyRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setMarketplace(mws.getMarketplace());
        request.setMarketplaceId(mws.getMarketplaceId());
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
