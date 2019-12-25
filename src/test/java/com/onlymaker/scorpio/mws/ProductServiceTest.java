package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.products.model.GetMatchingProductForIdResponse;
import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.regex.Matcher;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class ProductServiceTest {
    private ProductService productService;
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;

    @Before
    public void setup() {
        productService = new ProductService(appInfo, amazon.getList().get(0));
    }

    @Test
    public void getProduct() {
        GetMatchingProductForIdResponse response = productService.getMatchingProduct("B07L2WJXR5", "B07KPRM5Z4");
        response.getGetMatchingProductForIdResult().forEach(r -> {
            r.getProducts().getProduct().forEach(product -> {
                System.out.println("==========" + product.getIdentifiers().getMarketplaceASIN().getASIN() + "==========");
                Matcher matcher = Utils.MATCHING_PRODUCT_ASIN.matcher(product.getRelationships().toXMLFragment());
                while (matcher.find()) {
                    System.out.println("ASIN: " + matcher.group("asin"));
                }
                Matcher attrMatcher = Utils.MATCHING_PRODUCT_ATTR.matcher(product.getAttributeSets().toXMLFragment());
                while (attrMatcher.find()) {
                    System.out.println(String.format("%s: %s", attrMatcher.group("name"), attrMatcher.group("value")));
                }
                Matcher rankMatcher = Utils.MATCHING_PRODUCT_RANK.matcher(product.getSalesRankings().toXMLFragment());
                while (rankMatcher.find()) {
                    System.out.println(String.format("%s: %s", rankMatcher.group("name"), rankMatcher.group("rank")));
                }
            });
        });
    }
}
