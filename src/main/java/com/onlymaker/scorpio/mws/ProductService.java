package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.products.MarketplaceWebServiceProductsAsyncClient;
import com.amazonservices.mws.products.MarketplaceWebServiceProductsClient;
import com.amazonservices.mws.products.MarketplaceWebServiceProductsConfig;
import com.amazonservices.mws.products.model.*;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class ProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private MarketplaceWebServiceProductsClient client;
    private AppInfo appInfo;
    private MarketWebService mws;

    public ProductService(AppInfo appInfo, MarketWebService mws) {
        this.appInfo = appInfo;
        this.mws = mws;
    }

    public MarketWebService getMws() {
        return mws;
    }

    /**
     * @see <a href="http://docs.developer.amazonservices.com/en_US/products/Products_GetMatchingProductForId.html</a>
     * maximum request quota: 20
     * restore rate: 2/1s
     */
    public GetMatchingProductForIdResponse getMatchingProduct(String... asin) {
        GetMatchingProductForIdRequest request = new GetMatchingProductForIdRequest();
        request.setMarketplaceId(mws.getMarketplaceId());
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setIdType("ASIN");
        IdListType list = new IdListType();
        request.withIdList(list.withId(asin));
        forceWaiting();
        return getClient().getMatchingProductForId(request);
    }

    public GetMatchingProductForIdResponse getMatchingProduct(List<String> list) {
        GetMatchingProductForIdRequest request = new GetMatchingProductForIdRequest();
        request.setMarketplaceId(mws.getMarketplaceId());
        request.setSellerId(mws.getSellerId());
        request.setMWSAuthToken(mws.getAuthToken());
        request.setIdType("ASIN");
        IdListType idListType = new IdListType();
        idListType.setId(list);
        request.setIdList(idListType);
        forceWaiting();
        return getClient().getMatchingProductForId(request);
    }

    public Map<String, Map<String, String>> getProductInfo(String parentAsin) {
        List<String> children = new ArrayList<>();
        Map<String, Map<String, String>> productInfo = new HashMap<>();
        GetMatchingProductForIdResponse response = getMatchingProduct(parentAsin);
        List<GetMatchingProductForIdResult> results = response.getGetMatchingProductForIdResult();
        if (!results.isEmpty()) {
            ProductList productList = results.get(0).getProducts();
            if (productList == null || productList.getProduct().isEmpty()) {
                LOGGER.info("entry %s is invalid, no matching product", parentAsin);
                return null;
            } else {
                List<Product> products = results.get(0).getProducts().getProduct();
                Product product = products.get(0);
                String xml = product.getRelationships().toXMLFragment();
                if (xml.contains("VariationChild")) {
                    Matcher asinMatcher = Utils.MATCHING_PRODUCT_ASIN.matcher(xml);
                    while (asinMatcher.find()) {
                        children.add(asinMatcher.group("asin"));
                    }
                    while (!children.isEmpty()) {
                        int max = 5;
                        List<String> id = new ArrayList<>();
                        while (max > 0 && children.size() > 0) {
                            id.add(children.remove(0));
                            max--;
                        }
                        GetMatchingProductForIdResponse second = getMatchingProduct(id);
                        second.getGetMatchingProductForIdResult().forEach(result -> {
                            Map<String, String> attr = new HashMap();
                            Product child = result.getProducts().getProduct().get(0);
                            String asin = child.getIdentifiers().getMarketplaceASIN().getASIN();
                            Matcher attrMatcher = Utils.MATCHING_PRODUCT_ATTR.matcher(child.getAttributeSets().toXMLFragment());
                            while (attrMatcher.find()) {
                                attr.put(attrMatcher.group("name"), attrMatcher.group("value"));
                            }
                            Matcher rankMatcher = Utils.MATCHING_PRODUCT_RANK.matcher(child.getSalesRankings().toXMLFragment());
                            while (rankMatcher.find()) {
                                attr.put(rankMatcher.group("name"), rankMatcher.group("rank"));
                            }
                            productInfo.put(asin, attr);
                        });
                    }
                } else {
                    LOGGER.info("entry %s is invalid, maybe not a parent asin", parentAsin);
                    return null;
                }
            }
        }
        return productInfo;
    }

    private synchronized MarketplaceWebServiceProductsClient getClient() {
        if (client == null) {
            MarketplaceWebServiceProductsConfig config = new MarketplaceWebServiceProductsConfig();
            config.setServiceURL(mws.getMarketplaceUrl());
            client = new MarketplaceWebServiceProductsAsyncClient(
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

    private void forceWaiting() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
