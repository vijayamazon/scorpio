package com.onlymaker.scorpio.mws;

import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.data.AmazonOrderItem;
import com.onlymaker.scorpio.data.AmazonOrderItemRepository;
import com.onlymaker.scorpio.data.AmazonProduct;
import com.onlymaker.scorpio.data.AmazonProductRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class ProductServiceTest {
    private ProductService productService;
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;
    @Autowired
    AmazonProductRepository amazonProductRepository;
    @Autowired
    AmazonOrderItemRepository amazonOrderItemRepository;

    @Before
    public void setup() {
        productService = new ProductService(appInfo, amazon.getList().get(0));
    }

    @Test
    public void getProduct() {
        String market = productService.getMws().getMarketplace();
        String parent = "B07QWWBXRQ";
        System.out.println(market + " asin: " + parent);
        productService.getProductInfo(parent).forEach(((child, info) -> {
            System.out.println(market + " child: " + child);
            AmazonProduct product = amazonProductRepository.findByMarketAndAsin(market, child);
            if (product == null) {
                product = new AmazonProduct();
                product.setMarket(market);
                product.setAsin(child);
                String sellerSku = info.get("Model");
                if (StringUtils.isNotEmpty(sellerSku)) {
                    Map<String, String> result = Utils.parseSellerSku(sellerSku);
                    product.setSellerSku(sellerSku);
                    product.setSku(result.get("sku"));
                    product.setSize(result.get("size"));
                } else {
                    product.setSize(info.get("Size"));
                }
            }
            product.setParent(parent);
            product.setTitle(info.get("Title"));
            product.setImage(info.get("URL"));
            product.setColor(info.get("Color"));
            amazonProductRepository.save(product);
        }));
    }

    @Test
    public void setSku() {
        amazonProductRepository.findBySkuOrSkuIsNull("").forEach(product -> {
            AmazonOrderItem item = amazonOrderItemRepository.findTopByAsinOrderByPurchaseDateDesc(product.getAsin());
            if (item != null) {
                String sellerSku = item.getSellerSku();
                String sku = item.getSku();
                String size = item.getSize();
                if (StringUtils.isNotEmpty(sku)) {
                    System.out.println(String.format("%s set %s, %s, %s", item.getAsin(), sellerSku, sku, size));
                    product.setSellerSku(sellerSku);
                    product.setSku(sku);
                    product.setSize(size);
                    amazonProductRepository.save(product);
                }
            }
        });
    }
}
