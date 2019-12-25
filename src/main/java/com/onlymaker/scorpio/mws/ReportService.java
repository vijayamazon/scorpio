package com.onlymaker.scorpio.mws;

import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.*;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;

import java.io.OutputStream;
import java.util.*;

public class ReportService {
    public static final Map<String, String> REPORT_TYPE = new HashMap<String, String>() {{
        put("age", "_GET_FBA_INVENTORY_AGED_DATA_");
        put("fba_return", "_GET_FBA_FULFILLMENT_CUSTOMER_RETURNS_DATA_");
        put("inventory", "_GET_FBA_MYI_UNSUPPRESSED_INVENTORY_DATA_");
        put("receipt", "_GET_FBA_FULFILLMENT_INVENTORY_RECEIPTS_DATA_");
    }};
    private MarketplaceWebServiceClient client;
    private AppInfo appInfo;
    private MarketWebService mws;

    public ReportService(AppInfo appInfo, MarketWebService mws) {
        this.appInfo = appInfo;
        this.mws = mws;
    }

    /**
     * @see <a href="http://docs.developer.amazonservices.com/en_US/reports/Reports_RequestReport.html">Reports_RequestReport</a>
     * maximum request quota: 15
     * restore rate: 1/min
     * hourly request quota: 60
     */
    public RequestReportResponse requestReport(String reportType) throws MarketplaceWebServiceException {
        RequestReportRequest request = new RequestReportRequest();
        request.setMerchant(mws.getSellerId());
        request.setMarketplaceIdList(new IdList(Arrays.asList(mws.getMarketplaceId())));
        request.setMWSAuthToken(mws.getAuthToken());
        request.withReportType(reportType);
        return getClient().requestReport(request);
    }

    /**
     * @see <a href="http://docs.developer.amazonservices.com/en_US/reports/Reports_GetReportRequestList.html">Reports_GetReportRequestList</a>
     * maximum request quota: 10
     * restore rate: 1/45s
     * hourly request quota: 80
     */
    public GetReportRequestListResponse getReportRequestList(String reportType) throws MarketplaceWebServiceException {
        GetReportRequestListRequest request = new GetReportRequestListRequest();
        request.setMerchant(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setReportTypeList(new TypeList(Arrays.asList(reportType)));
        return getClient().getReportRequestList(request);
    }

    public GetReportRequestListResponse getReportRequestListCompleted(String reportType) throws MarketplaceWebServiceException {
        GetReportRequestListRequest request = new GetReportRequestListRequest();
        request.setMerchant(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setReportTypeList(new TypeList(Arrays.asList(reportType)));
        List<String> completedStatus = new ArrayList<>();
        completedStatus.add("_CANCELLED_");
        completedStatus.add("_DONE_");
        completedStatus.add("_DONE_NO_DATA_");
        request.setReportProcessingStatusList(new StatusList(completedStatus));
        return getClient().getReportRequestList(request);
    }

    public GetReportRequestListByNextTokenResponse getReportRequestListByNextToken(String nextToken) throws MarketplaceWebServiceException {
        GetReportRequestListByNextTokenRequest request = new GetReportRequestListByNextTokenRequest();
        request.setMerchant(mws.getSellerId());
        request.setNextToken(nextToken);
        request.setMWSAuthToken(mws.getAuthToken());
        return getClient().getReportRequestListByNextToken(request);
    }

    /**
     * @see <a href="https://docs.developer.amazonservices.com/en_US/reports/Reports_GetReportList.html">Reports_GetReportList</a>
     * maximum request quota: 10
     * restore rate: 1/45s
     * hourly request quota: 80
     */
    public GetReportListResponse getReportList(String reportType) throws MarketplaceWebServiceException {
        GetReportListRequest request = new GetReportListRequest();
        request.setMerchant(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setReportTypeList(new TypeList(Arrays.asList(reportType)));
        return getClient().getReportList(request);
    }

    public GetReportListByNextTokenResponse getReportListByNextToken(String nextToken) throws MarketplaceWebServiceException {
        forceWaiting();
        GetReportListByNextTokenRequest request = new GetReportListByNextTokenRequest();
        request.setMerchant(mws.getSellerId());
        request.setNextToken(nextToken);
        request.setMWSAuthToken(mws.getAuthToken());
        return getClient().getReportListByNextToken(request);
    }

    public GetReportRequest prepareGetReport(String id, OutputStream outputStream) {
        GetReportRequest request = new GetReportRequest();
        request.setMerchant(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setReportId(id);
        request.setReportOutputStream(outputStream);
        return request;
    }

    public GetReportResponse getReport(GetReportRequest request) throws MarketplaceWebServiceException {
        return getClient().getReport(request);
    }

    private MarketplaceWebServiceClient getClient() {
        if (client == null) {
            MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
            config.setServiceURL(mws.getMarketplaceUrl());
            client = new MarketplaceWebServiceClient(mws.getAccessKey(),
                    mws.getSecretKey(),
                    appInfo.getName(),
                    appInfo.getVersion(),
                    config
            );
        }
        return client;
    }

    private void forceWaiting() {
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
