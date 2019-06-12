package com.onlymaker.scorpio.task;

import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.data.*;
import com.onlymaker.scorpio.mws.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

//@Service
public class AmazonRunOnce {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonRunOnce.class);
    private static final int PAGE_SIZE = 100;
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;
    @Autowired
    AmazonSellerSkuRepository amazonSellerSkuRepository;
    @Autowired
    AmazonInboundItemRepository amazonInboundItemRepository;
    @Autowired
    AmazonOrderItemRepository amazonOrderItemRepository;
    @Autowired
    AmazonInventoryRepository amazonInventoryRepository;

    //@Scheduled(cron = "0 0 11 11 6 ?")
    public void parse() {
        LOGGER.info("Run onetime task ...");
        refreshSellerSkuSize();
        refreshInventorySkuSize();
        refreshInboundItemSkuSize();
        refreshOrderItemSkuSize();
    }

    private void refreshSellerSkuSize() {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        Page<AmazonSellerSku> page = amazonSellerSkuRepository.findAll(pageable);
        LOGGER.debug("Total seller sku pages: {}", page.getTotalPages());
        processAmazonSellerSkuPage(page);
        while (page.hasNext()) {
            page = amazonSellerSkuRepository.findAll(page.nextPageable());
            processAmazonSellerSkuPage(page);
        }
    }

    private void processAmazonSellerSkuPage(Page<AmazonSellerSku> page) {
        page.forEach(sellerSku -> {
            LOGGER.debug("parse sellerSku: {}", sellerSku.getSellerSku());
            Map<String, String> map = Utils.parseSellerSku(sellerSku.getSellerSku());
            sellerSku.setSku(map.get("sku"));
            sellerSku.setSize(map.get("size"));
            amazonSellerSkuRepository.save(sellerSku);
        });
    }

    private void refreshInventorySkuSize() {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        Page<AmazonInventory> page = amazonInventoryRepository.findAll(pageable);
        LOGGER.debug("Total inventory pages: {}", page.getTotalPages());
        processAmazonInventoryPage(page);
        while (page.hasNext()) {
            page = amazonInventoryRepository.findAll(page.nextPageable());
            processAmazonInventoryPage(page);
        }
    }

    private void processAmazonInventoryPage(Page<AmazonInventory> page) {
        page.forEach(inventory -> {
            LOGGER.debug("parse inventory: {}", inventory.getSellerSku());
            Map<String, String> map = Utils.parseSellerSku(inventory.getSellerSku());
            inventory.setSku(map.get("sku"));
            inventory.setSize(map.get("size"));
            amazonInventoryRepository.save(inventory);
        });
    }

    private void refreshInboundItemSkuSize() {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        Page<AmazonInboundItem> page = amazonInboundItemRepository.findAll(pageable);
        LOGGER.debug("Total inbound pages: {}", page.getTotalPages());
        processAmazonInboundItemPage(page);
        while (page.hasNext()) {
            page = amazonInboundItemRepository.findAll(page.nextPageable());
            processAmazonInboundItemPage(page);
        }
    }

    private void processAmazonInboundItemPage(Page<AmazonInboundItem> page) {
        page.forEach(inbound -> {
            LOGGER.debug("parse inbound: {}", inbound.getSellerSku());
            Map<String, String> map = Utils.parseSellerSku(inbound.getSellerSku());
            inbound.setSku(map.get("sku"));
            inbound.setSize(map.get("size"));
            amazonInboundItemRepository.save(inbound);
        });
    }

    private void refreshOrderItemSkuSize() {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        Page<AmazonOrderItem> page = amazonOrderItemRepository.findAll(pageable);
        LOGGER.debug("Total order pages: {}", page.getTotalPages());
        processAmazonOrderItemPage(page);
        while (page.hasNext()) {
            page = amazonOrderItemRepository.findAll(page.nextPageable());
            processAmazonOrderItemPage(page);
        }
    }

    private void processAmazonOrderItemPage(Page<AmazonOrderItem> page) {
        page.forEach(order -> {
            LOGGER.debug("parse order: {}", order.getSellerSku());
            Map<String, String> map = Utils.parseSellerSku(order.getSellerSku());
            order.setSku(map.get("sku"));
            order.setSize(map.get("size"));
            amazonOrderItemRepository.save(order);
        });
    }
}
