package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.client.MwsUtl;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersAsyncClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersRequest;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.datatype.XMLGregorianCalendar;
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
        XMLGregorianCalendar queryStartDateTime = MwsUtl.getDTF().newXMLGregorianCalendar();
        queryStartDateTime.setYear(configuration.getInventoryQueryStartYear());
        queryStartDateTime.setMonth(configuration.getInventoryQueryStartMonth());
        queryStartDateTime.setDay(configuration.getInventoryQueryStartDay());
        queryStartDateTime.setHour(0);
        queryStartDateTime.setMinute(0);
        queryStartDateTime.setSecond(0);
        queryStartDateTime.setMillisecond(0);
        request.setOrderStatus(Collections.singletonList("Shipped"));
        request.setCreatedAfter(queryStartDateTime);
        return getClient().listOrders(request);
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
                    config, (ExecutorService)null);
        }

        return client;
    }
}
