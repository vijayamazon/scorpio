package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import com.onlymaker.scorpio.data.AmazonInbound;
import com.onlymaker.scorpio.data.AmazonInboundItem;
import com.onlymaker.scorpio.data.AmazonInboundItemRepository;
import com.onlymaker.scorpio.data.AmazonInboundRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class InboundServiceTest {
    private InboundService inboundService;
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;
    @Autowired
    AmazonInboundRepository amazonInboundRepository;
    @Autowired
    AmazonInboundItemRepository amazonInboundItemRepository;

    @Before
    public void setup() {
        inboundService = new InboundService(appInfo, amazon.getList().get(5));
    }

    /**
     * InboundShipmentInfo:
     * {
     * "shipmentId" : "FBA15GMTRLNV",
     * "shipmentName" : "周总OMS4件48双海运-3.23大货",
     * "shipFromAddress" : {
     * "name" : "Rao jinyong",
     * "addressLine1" : "Jinhua Town Henger Street No.4",
     * "addressLine2" : null,
     * "districtOrCounty" : null,
     * "city" : "Chengdu",
     * "stateOrProvinceCode" : "Sichuan",
     * "countryCode" : "CN",
     * "postalCode" : "610000",
     * "setName" : true,
     * "setAddressLine1" : true,
     * "setAddressLine2" : false,
     * "setDistrictOrCounty" : false,
     * "setCity" : true,
     * "setStateOrProvinceCode" : true,
     * "setCountryCode" : true,
     * "setPostalCode" : true
     * },
     * "destinationFulfillmentCenterId" : "IND4",
     * "shipmentStatus" : "RECEIVING",
     * "labelPrepType" : "SELLER_LABEL",
     * "areCasesRequired" : false,
     * "confirmedNeedByDate" : null,
     * "boxContentsSource" : "FEED",
     * "estimatedBoxContentsFee" : null,
     * "setShipmentId" : true,
     * "setShipmentName" : true,
     * "setShipFromAddress" : true,
     * "setDestinationFulfillmentCenterId" : true,
     * "setShipmentStatus" : true,
     * "setLabelPrepType" : true,
     * "setAreCasesRequired" : true,
     * "setConfirmedNeedByDate" : false,
     * "setBoxContentsSource" : true,
     * "setEstimatedBoxContentsFee" : false
     * }
     */
    @Test
    public void listInboundShipments() {
        ListInboundShipmentsResult result = inboundService
                .getListInboundShipmentsResponseByStatus(AmazonInbound.IN_PROGRESS_STATUS_LIST)
                .getListInboundShipmentsResult();
        result.getShipmentData().getMember().forEach(this::print);
        if (result.isSetNextToken()) {
            System.out.println("nextToken:\n" + result.getNextToken());
            ListInboundShipmentsByNextTokenResult nextResult = inboundService
                    .getListInboundShipmentsResponseByNextToken(result.getNextToken())
                    .getListInboundShipmentsByNextTokenResult();
            List<InboundShipmentInfo> list = nextResult.getShipmentData().getMember();
            System.out.println(String.format("Size %d, nextToken: %s", list.size(), nextResult.getNextToken()));
        }
    }

    @Test
    public void listInboundShipmentsLastDay() {
        inboundService
                .getListInboundShipmentsResponseUpdatedLastDay()
                .getListInboundShipmentsResult()
                .getShipmentData()
                .getMember()
                .forEach(this::print);
    }

    /**
     * InboundShipmentItem:
     * {
     * "shipmentId" : "FBA15GMTRLNV",
     * "sellerSKU" : "T17010a-US11-FBA-X0",
     * "fulfillmentNetworkSKU" : "X0022URAOB",
     * "quantityShipped" : 2,
     * "quantityReceived" : 2,
     * "quantityInCase" : 0,
     * "releaseDate" : null,
     * "prepDetailsList" : {
     * "prepDetails" : [ {
     * "prepInstruction" : "Labeling",
     * "prepOwner" : "SELLER",
     * "setPrepInstruction" : true,
     * "setPrepOwner" : true
     * } ],
     * "setPrepDetails" : true
     * },
     * "setSellerSKU" : true,
     * "setQuantityShipped" : true,
     * "setShipmentId" : true,
     * "setFulfillmentNetworkSKU" : true,
     * "setQuantityReceived" : true,
     * "setQuantityInCase" : true,
     * "setReleaseDate" : false,
     * "setPrepDetailsList" : true
     * }
     */
    @Test
    public void listInboundShipmentItems() {
        String id = "FBA15GMTRLNV";
        ListInboundShipmentItemsResult result = inboundService
                .getListInboundShipmentItemsResponse(id)
                .getListInboundShipmentItemsResult();
        result.getItemData().getMember().forEach(this::print);
        if (result.isSetNextToken()) {
            System.out.println("nextToken:\n" + result.getNextToken());
            ListInboundShipmentItemsByNextTokenResult nextResult = inboundService
                    .getListInboundShipmentItemsByNextTokenResponse(result.getNextToken())
                    .getListInboundShipmentItemsByNextTokenResult();
            List<InboundShipmentItem> list = nextResult.getItemData().getMember();
            System.out.println(String.format("Size %d, nextToken: %s", list.size(), nextResult.getNextToken()));
        }
    }


    @Test
    public void refreshInbound() {
        System.out.println(inboundService.getMws().getMarketplace());
        LocalDate date = LocalDate.now();
        ListInboundShipmentsResult result = inboundService
                .getListInboundShipmentsResponseUpdatedBetween(date.minusDays(90), date.plusDays(1))
                .getListInboundShipmentsResult();
        result.getShipmentData().getMember().forEach(this::updateInboundStatus);
        String token = result.getNextToken();
        while (StringUtils.isNotEmpty(token)) {
            ListInboundShipmentsByNextTokenResult nextResult = inboundService
                    .getListInboundShipmentsResponseByNextToken(token)
                    .getListInboundShipmentsByNextTokenResult();
            nextResult.getShipmentData().getMember().forEach(this::updateInboundStatus);
            token = nextResult.getNextToken();
        }
    }

    private void updateInboundStatus(InboundShipmentInfo shipment) {
        AmazonInbound inbound = amazonInboundRepository.findByShipmentId(shipment.getShipmentId());
        if (inbound != null && !Objects.equals(inbound.getStatus(), shipment.getShipmentStatus())) {
            System.out.println("Updating shipment " + shipment.getShipmentName() + ", status: " + inbound.getStatus() + " -> " + shipment.getShipmentStatus());
            inbound.setStatus(shipment.getShipmentStatus());
            inbound.setData(Utils.getJsonString(shipment));
            amazonInboundRepository.save(inbound);
            Iterable<AmazonInboundItem> iterable = amazonInboundItemRepository.findAllByShipmentId(shipment.getShipmentId());
            for (AmazonInboundItem item : iterable) {
                item.setStatus(shipment.getShipmentStatus());
                amazonInboundItemRepository.save(item);
            }
        }
    }

    private void print(Object o) {
        try {
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
