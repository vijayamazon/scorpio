package com.onlymaker.scorpio.task;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.config.MarketWebService;
import com.onlymaker.scorpio.data.*;
import com.onlymaker.scorpio.mws.InboundService;
import com.onlymaker.scorpio.mws.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
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
    AmazonInboundRepository amazonInboundRepository;
    @Autowired
    AmazonInboundItemRepository amazonInboundItemRepository;

    @Scheduled(cron = "0 10 7 6 6 ?")
    public void parse() {
        LOGGER.info("Run onetime task ...");
        initInboundData();
        initSkuSize();
    }

    private void initSkuSize() {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        Page<AmazonSellerSku> paeg = amazonSellerSkuRepository.findAll(pageable);
        LOGGER.debug("Total seller sku pages: {}", paeg.getTotalPages());
        processAmazonSellerSkuPage(paeg);
        while (paeg.hasNext()) {
            paeg = amazonSellerSkuRepository.findAll(paeg.nextPageable());
            processAmazonSellerSkuPage(paeg);
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

    private void initInboundData() {
        List<MarketWebService> list = amazon.getList();
        for (MarketWebService mws : list) {
            InboundService inboundService = new InboundService(appInfo, mws);
            ListInboundShipmentsResult result = inboundService
                    .getListInboundShipmentsResponseByStatus(AmazonInbound.COUNTING_STATUS_LIST)
                    .getListInboundShipmentsResult();
            result.getShipmentData().getMember().forEach(member -> processInboundShipmentInfo(member, mws.getMarketplace(), inboundService));
            String token = result.getNextToken();
            while (StringUtils.isNotEmpty(token)) {
                ListInboundShipmentsByNextTokenResult nextResult = inboundService
                        .getListInboundShipmentsResponseByNextToken(token)
                        .getListInboundShipmentsByNextTokenResult();
                nextResult.getShipmentData().getMember().forEach(member -> processInboundShipmentInfo(member, mws.getMarketplace(), inboundService));
                token = nextResult.getNextToken();
            }
        }
    }

    private void processInboundShipmentInfo(InboundShipmentInfo info, String market, InboundService inboundService) {
        String shipmentId = info.getShipmentId();
        AmazonInbound amazonInbound = amazonInboundRepository.findByShipmentId(shipmentId);
        if (amazonInbound == null) {
            LOGGER.info("Saving shipment {}, status {}", info.getShipmentName(), info.getShipmentStatus());
            amazonInbound = new AmazonInbound(info);
            amazonInbound.setMarket(market);
            amazonInbound.setDest(Utils.getDestFromMarket(market));
            amazonInboundRepository.save(amazonInbound);
            ListInboundShipmentItemsResult result = inboundService
                    .getListInboundShipmentItemsResponse(shipmentId)
                    .getListInboundShipmentItemsResult();
            for (InboundShipmentItem inboundShipmentItem : result.getItemData().getMember()) {
                processInboundShipmentItem(amazonInbound, inboundShipmentItem);
            }
            String token = result.getNextToken();
            while (StringUtils.isNotEmpty(token)) {
                ListInboundShipmentItemsByNextTokenResult nextResult = inboundService
                        .getListInboundShipmentItemsByNextTokenResponse(token)
                        .getListInboundShipmentItemsByNextTokenResult();
                for (InboundShipmentItem member : nextResult.getItemData().getMember()) {
                    processInboundShipmentItem(amazonInbound, member);
                }
                token = nextResult.getNextToken();
            }
        }
    }

    private void processInboundShipmentItem(AmazonInbound inbound, InboundShipmentItem item) {
        LOGGER.info("Saving shipment item {}", item.getSellerSKU());
        AmazonInboundItem amazonInboundItem = new AmazonInboundItem(inbound, item);
        Map<String, String> map = Utils.parseSellerSku(item.getSellerSKU());
        amazonInboundItem.setSku(map.get("sku"));
        amazonInboundItem.setSize(map.get("size"));
        amazonInboundItemRepository.save(amazonInboundItem);
    }
}
