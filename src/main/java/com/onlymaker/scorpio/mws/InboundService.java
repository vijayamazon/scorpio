package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSAsyncClient;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;
import com.onlymaker.scorpio.data.AmazonInbound;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InboundService {
    private static final long FETCH_INBOUND_INTERVAL_IN_MS = 5000;
    private FBAInboundServiceMWSAsyncClient client;
    private AppInfo appInfo;
    private MarketWebService mws;

    public InboundService(AppInfo appInfo, MarketWebService mws) {
        this.appInfo = appInfo;
        this.mws = mws;
    }

    public MarketWebService getMws() {
        return mws;
    }

    /**
     * @see <a href="http://docs.developer.amazonservices.com/en_US/fba_inbound/FBAInbound_ListInboundShipments.html">FBAInbound_ListInboundShipments.html</a>
     * maximum request quota: 30
     * restore rate: 2/1s
     */
    public ListInboundShipmentsResponse getListInboundShipmentsResponseByStatus(List<String> status) {
        forceWaiting(FETCH_INBOUND_INTERVAL_IN_MS);
        ListInboundShipmentsRequest request = new ListInboundShipmentsRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        ShipmentStatusList shipmentStatusList = new ShipmentStatusList(status);
        request.setShipmentStatusList(shipmentStatusList);
        return getClient().listInboundShipments(request);
    }

    public ListInboundShipmentsResponse getListInboundShipmentsResponseUpdatedLastDay() {
        LocalDate date = LocalDate.now();
        return getListInboundShipmentsResponseUpdatedBetween(date.minusDays(1), date.plusDays(1));
    }

    public ListInboundShipmentsResponse getListInboundShipmentsResponseUpdatedBetween(LocalDate after, LocalDate before) {
        forceWaiting(FETCH_INBOUND_INTERVAL_IN_MS);
        ListInboundShipmentsRequest request = new ListInboundShipmentsRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        List<String> status = new ArrayList<>();
        status.addAll(AmazonInbound.IN_PROGRESS_STATUS_LIST);
        status.addAll(AmazonInbound.FINAL_STATUS_LIST);
        ShipmentStatusList shipmentStatusList = new ShipmentStatusList(status);
        request.setShipmentStatusList(shipmentStatusList);
        request.setLastUpdatedAfter(Utils.getXMLGregorianCalendar(after));
        request.setLastUpdatedBefore(Utils.getXMLGregorianCalendar(before));
        return getClient().listInboundShipments(request);
    }

    public ListInboundShipmentsByNextTokenResponse getListInboundShipmentsResponseByNextToken(String token) {
        forceWaiting(FETCH_INBOUND_INTERVAL_IN_MS);
        ListInboundShipmentsByNextTokenRequest request = new ListInboundShipmentsByNextTokenRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setNextToken(token);
        return getClient().listInboundShipmentsByNextToken(request);
    }

    public ListInboundShipmentItemsResponse getListInboundShipmentItemsResponse(String shipmentId) {
        forceWaiting(FETCH_INBOUND_INTERVAL_IN_MS);
        ListInboundShipmentItemsRequest request = new ListInboundShipmentItemsRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setShipmentId(shipmentId);
        return getClient().listInboundShipmentItems(request);
    }

    public ListInboundShipmentItemsByNextTokenResponse getListInboundShipmentItemsByNextTokenResponse(String token) {
        forceWaiting(FETCH_INBOUND_INTERVAL_IN_MS);
        ListInboundShipmentItemsByNextTokenRequest request = new ListInboundShipmentItemsByNextTokenRequest();
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setNextToken(token);
        return getClient().listInboundShipmentItemsByNextToken(request);
    }

    private synchronized FBAInboundServiceMWSAsyncClient getClient() {
        if (client == null) {
            FBAInboundServiceMWSConfig config = new FBAInboundServiceMWSConfig();
            config.setServiceURL(mws.getMarketplaceUrl());
            client = new FBAInboundServiceMWSAsyncClient(
                    mws.getAccessKey(),
                    mws.getSecretKey(),
                    appInfo.getName(),
                    appInfo.getVersion(),
                    config,
                    null
            );
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
