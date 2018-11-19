package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.*;
import com.onlymaker.scorpio.Main;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class InventoryServiceTest {
    @Autowired
    InventoryService inventoryService;

    @Test
    public void status() {
        GetServiceStatusResponse statusResponse = inventoryService.getServiceStatusResponse();
        GetServiceStatusResult statusResult = statusResponse.getGetServiceStatusResult();
        System.out.println("status:" + statusResult.getStatus());
    }

    @Test
    public void queryBySku() {
        ListInventorySupplyResponse response = inventoryService.getListInventorySupplyResponse(inventoryService.buildRequestWithSku("HPUS-P8804B-US15-FBA", "AHUS-HJ1708182-US5-FBA"));
        List<InventorySupply> list = response.getListInventorySupplyResult()
                .getInventorySupplyList()
                .getMember();
        printStock(list);
    }

    @Test
    public void queryByDate() throws InterruptedException {
        ListInventorySupplyResponse response = inventoryService.getListInventorySupplyResponse(inventoryService.buildRequestWithDate());
        List<InventorySupply> list = response.getListInventorySupplyResult()
                .getInventorySupplyList()
                .getMember();
        printStock(list);
        String nextToken = response.getListInventorySupplyResult().getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("next: " + nextToken);
            ListInventorySupplyByNextTokenResponse nextResponse = inventoryService.getListInventorySupplyByNextTokenResponse(nextToken);
            printStock(nextResponse.getListInventorySupplyByNextTokenResult().getInventorySupplyList().getMember());
            nextToken = nextResponse.getListInventorySupplyByNextTokenResult().getNextToken();
        }
    }

    private void printStock(List<InventorySupply> list) {
        list.forEach(i -> {
            System.out.println(String.format("asin: %s, sku: %s, quantity:", i.getASIN(), i.getSellerSKU()));
            System.out.println(String.format("    totalSupply %d, inStockSupply %d", i.getTotalSupplyQuantity(), i.getInStockSupplyQuantity()));
        });
    }
}
