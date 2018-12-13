package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSAsyncClient;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSClient;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.amazonservices.mws.FulfillmentInventory._2010_10_01.samples.ListInventorySupplySample.invokeListInventorySupply;

/**
 * @see <a href="https://docs.developer.amazonservices.com/en_US/fba_inventory/FBAInventory_ListInventorySupply.html">FBAInventory_ListInventorySupply.html</a>
 * maximum request quota of 30
 * two requests every second
 */
@Service
public class InventoryService {
    /** The client, lazy initialized. Async client is also a sync client. */
    private static FBAInventoryServiceMWSAsyncClient client;
    @Autowired
    Configuration configuration;

    public GetServiceStatusResponse getServiceStatusResponse() {
        GetServiceStatusRequest request = new GetServiceStatusRequest();
        request.setSellerId(configuration.getSellerId());
        request.setMWSAuthToken(configuration.getAuthToken());
        request.setMarketplace(configuration.getMarketplace());
        return getClient().getServiceStatus(request);
    }

    public ListInventorySupplyResponse getListInventorySupplyResponse(ListInventorySupplyRequest request) {
        return invokeListInventorySupply(getClient(), request);
    }

    public ListInventorySupplyByNextTokenResponse getListInventorySupplyByNextTokenResponse(String nextToken) {
        ListInventorySupplyByNextTokenRequest request = new ListInventorySupplyByNextTokenRequest();
        request.setSellerId(configuration.getSellerId());
        request.setMWSAuthToken(configuration.getAuthToken());
        request.setMarketplace(configuration.getMarketplace());
        request.setNextToken(nextToken);
        return getClient().listInventorySupplyByNextToken(request);
    }

    public ListInventorySupplyRequest buildRequestWithSku(String ... sku) {
        ListInventorySupplyRequest request = new ListInventorySupplyRequest();
        request.setSellerId(configuration.getSellerId());
        request.setMWSAuthToken(configuration.getAuthToken());
        request.setMarketplace(configuration.getMarketplace());
        request.setMarketplaceId(configuration.getMarketplaceId());
        SellerSkuList sellerSkus = new SellerSkuList();
        sellerSkus.withMember(sku);
        request.setSellerSkus(sellerSkus);
        return request;
    }

    public ListInventorySupplyRequest buildRequestWithDate() {
        ListInventorySupplyRequest request = new ListInventorySupplyRequest();
        request.setSellerId(configuration.getSellerId());
        request.setMWSAuthToken(configuration.getAuthToken());
        request.setMarketplace(configuration.getMarketplace());
        request.setMarketplaceId(configuration.getMarketplaceId());
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
            config.setServiceURL(configuration.getMarketplaceUrl());
            client = new FBAInventoryServiceMWSAsyncClient(
                    configuration.getAccessKey(),
                    configuration.getSecretKey(),
                    configuration.getAppName(),
                    configuration.getAppVersion(),
                    config, null);
        }
        return client;
    }
}
