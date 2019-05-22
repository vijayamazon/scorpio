package com.onlymaker.scorpio.task;

import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.data.AmazonInventory;
import com.onlymaker.scorpio.data.AmazonInventoryRepository;
import com.onlymaker.scorpio.data.AmazonOrderItem;
import com.onlymaker.scorpio.data.AmazonOrderItemRepository;
import com.onlymaker.scorpio.mws.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;

@Service
public class AmazonSkuParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonSkuParser.class);
    private static final int PAGE_SIZE = 100;
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;
    @Autowired
    AmazonOrderItemRepository amazonOrderItemRepository;
    @Autowired
    AmazonInventoryRepository amazonInventoryRepository;

    @Scheduled(cron = "0 30 10 22 5 ?")
    public void parse() {
        LOGGER.info("parse seller sku ...");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Pageable orderPageable = PageRequest.of(0, PAGE_SIZE);
        Page<AmazonOrderItem> orderPage = amazonOrderItemRepository.findByCreateTimeBefore(timestamp, orderPageable);
        LOGGER.debug("parse order sku, total page {}", orderPage.getTotalPages());
        parseOrderPage(orderPage);
        while (orderPage.hasNext()) {
            orderPage = amazonOrderItemRepository.findByCreateTimeBefore(timestamp, orderPage.nextPageable());
            parseOrderPage(orderPage);
        }

        Pageable inventoryPageable = PageRequest.of(0, PAGE_SIZE);
        Page<AmazonInventory> inventoryPage = amazonInventoryRepository.findByCreateDateBefore(new Date(timestamp.getTime()), inventoryPageable);
        LOGGER.debug("parse inventory sku, total page {}", inventoryPage.getTotalPages());
        parseInventoryPage(inventoryPage);
        while (inventoryPage.hasNext()) {
            inventoryPage = amazonInventoryRepository.findByCreateDateBefore(new Date(timestamp.getTime()), inventoryPage.nextPageable());
            parseInventoryPage(inventoryPage);
        }
    }

    private void parseOrderPage(Page<AmazonOrderItem> page) {
        page.forEach(orderItem -> {
            LOGGER.debug("parse order sellerSku: {}", orderItem.getSellerSku());
            Map<String, String> map = Utils.parseSellerSku(orderItem.getSellerSku());
            orderItem.setSku(map.get("sku"));
            orderItem.setSize(map.get("size"));
            amazonOrderItemRepository.save(orderItem);
        });
    }

    private void parseInventoryPage(Page<AmazonInventory> page) {
        page.forEach(inventory -> {
            LOGGER.debug("parse inventory sellerSku: {}", inventory.getSellerSku());
            Map<String, String> map = Utils.parseSellerSku(inventory.getSellerSku());
            inventory.setSku(map.get("sku"));
            inventory.setSize(map.get("size"));
            amazonInventoryRepository.save(inventory);
        });
    }
}
