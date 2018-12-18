package com.onlymaker.scorpio.mws;

import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.*;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReportService {
    public static final Map<String, String> REPORT_TYPE = new HashMap<String, String>() {{
        put("order", "_GET_FLAT_FILE_ORDERS_DATA_");
        put("performance", "_GET_V1_SELLER_PERFORMANCE_REPORT_");
    }};
    private MarketplaceWebServiceClient client;
    private AppInfo appInfo;
    private MarketWebService mws;

    public ReportService(AppInfo appInfo, MarketWebService mws) {
        this.appInfo = appInfo;
        this.mws = mws;
    }

    /**
     * @see <a href="http://docs.developer.amazonservices.com/en_US/reports/Reports_RequestReport.html">Request Report</a>
     * less than 15 per minute
     * less than 60 per hour
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
     * less than 10 per minute
     * less than 80 per hour
     * @return
     * @throws MarketplaceWebServiceException
     */
    public GetReportListResponse getReportList(String reportType) throws MarketplaceWebServiceException {
        GetReportListRequest request = new GetReportListRequest();
        request.setMerchant(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setReportTypeList(new TypeList(Arrays.asList(reportType)));
        return getClient().getReportList(request);
    }

    public GetReportListByNextTokenResponse getReportListByNextToken(String nextToken) throws MarketplaceWebServiceException {
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
                    appInfo.getVersion(), config);
        }
        return client;
    }
}
