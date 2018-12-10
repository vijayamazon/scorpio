package com.onlymaker.scorpio.mws;

import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {
    private static MarketplaceWebServiceClient client;
    public static final Map<String, String> REPORT_TYPE = new HashMap<String, String>() {{
        put("order", "_GET_FLAT_FILE_ORDERS_DATA_");
        put("performance", "_GET_V1_SELLER_PERFORMANCE_REPORT_");
    }};
    @Autowired
    Configuration configuration;

    /**
     * @see <a href="http://docs.developer.amazonservices.com/en_US/reports/Reports_RequestReport.html">Request Report</a>
     * less than 15 per minute
     * less than 60 per hour
     */
    public RequestReportResponse requestReport(String reportType) throws MarketplaceWebServiceException {
        RequestReportRequest request = new RequestReportRequest();
        request.setMerchant(configuration.getSellerId());
        request.setMarketplaceIdList(new IdList(Arrays.asList(configuration.getMarketplaceId())));
        request.setMWSAuthToken(configuration.getAuthToken());
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
        request.setMerchant(configuration.getSellerId());
        request.setMWSAuthToken(configuration.getAuthToken());
        request.setReportTypeList(new TypeList(Arrays.asList(reportType)));
        return getClient().getReportList(request);
    }

    public GetReportListByNextTokenResponse getReportListByNextToken(String nextToken) throws MarketplaceWebServiceException {
        GetReportListByNextTokenRequest request = new GetReportListByNextTokenRequest();
        request.setMerchant(configuration.getSellerId());
        request.setNextToken(nextToken);
        request.setMWSAuthToken(configuration.getAuthToken());
        return getClient().getReportListByNextToken(request);
    }

    public GetReportRequest prepareGetReport(String id, OutputStream outputStream) {
        GetReportRequest request = new GetReportRequest();
        request.setMerchant(configuration.getSellerId());
        request.setMWSAuthToken(configuration.getAuthToken());
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
            config.setServiceURL(configuration.getMarketplaceUrl());
            client = new MarketplaceWebServiceClient(configuration.getAccessKey(),
                    configuration.getSecretKey(),
                    configuration.getAppName(),
                    configuration.getAppVersion(), config);
        }
        return client;
    }
}
