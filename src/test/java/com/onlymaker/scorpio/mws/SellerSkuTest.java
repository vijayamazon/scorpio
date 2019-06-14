package com.onlymaker.scorpio.mws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.data.AmazonSellerSku;
import com.onlymaker.scorpio.data.AmazonSellerSkuRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.Objects;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class SellerSkuTest {
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    AmazonSellerSkuRepository amazonSellerSkuRepository;

    @Test
    public void parse() throws JsonProcessingException {
        for (AmazonSellerSku seller : amazonSellerSkuRepository.findBySizeIsNullOrSizeEquals("")) {
            Map<String, String> map = Utils.parseSellerSku(seller.getSellerSku());
            System.out.println(seller.getSellerSku());
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
            String size = map.get("size");
            String sku = map.get("sku");
            if (!Objects.equals(size, sku)) {
                seller.setSku(sku);
                seller.setSize(size);
                amazonSellerSkuRepository.save(seller);
            }
        }
    }
}
