package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.*;
import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
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
    private InventoryService inventoryService;
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;

    @Before
    public void setup() {
        inventoryService = new InventoryService(appInfo, amazon.getList().get(0));
    }

    @Test
    public void status() {
        GetServiceStatusResponse statusResponse = inventoryService.getServiceStatusResponse();
        GetServiceStatusResult statusResult = statusResponse.getGetServiceStatusResult();
        System.out.println("status:" + statusResult.getStatus());
    }

    @Test
    public void queryBySku() {
        SellerSkuList sellerSkuList = new SellerSkuList();
        sellerSkuList.withMember("T17012a-US11", "test");
        List<InventorySupply> list = inventoryService
                .getListInventorySupplyResponseWithSku(sellerSkuList)
                .getListInventorySupplyResult()
                .getInventorySupplyList()
                .getMember();
        printStock(list);
    }

    @Test
    public void queryByDate() throws InterruptedException {
        ListInventorySupplyResponse response = inventoryService.getListInventorySupplyResponse(inventoryService.buildRequestWithinLastDay());
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

    @Test
    public void queryForEachStoreOnce() {
        for (MarketWebService mws : amazon.getList()) {
            try {
                System.out.println("==========" + mws.getSellerId() + "==========");
                System.out.println("==========" + mws.getAuthToken() + "==========");
                InventoryService service = new InventoryService(appInfo, mws);
                ListInventorySupplyResponse response = service.getListInventorySupplyResponse(service.buildRequestWithinLastDay());
                List<InventorySupply> list = response.getListInventorySupplyResult()
                        .getInventorySupplyList()
                        .getMember();
                if (list.size() != 0) {
                    printStock(list.get(0));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void printStock(List<InventorySupply> list) {
        list.forEach(this::printStock);
    }

    private void printStock(InventorySupply inventorySupply) {
        System.out.println(String.format("asin: %s, fnSku: %s, sku: %s, quantity:", inventorySupply.getASIN(), inventorySupply.getFNSKU(), inventorySupply.getSellerSKU()));
        System.out.println(String.format("    totalSupply %d, inStockSupply %d", inventorySupply.getTotalSupplyQuantity(), inventorySupply.getInStockSupplyQuantity()));
    }
}
