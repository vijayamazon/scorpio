package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class InboundServiceTest {
    private InboundService inboundService;
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;

    @Before
    public void setup() {
        inboundService = new InboundService(appInfo, amazon.getList().get(0));
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
        List<String> status = new ArrayList<String>() {{
            add("WORKING");
            add("SHIPPED");
            add("IN_TRANSIT");
            add("DELIVERED");
            add("CHECKED_IN");
            add("RECEIVING");
        }};
        ListInboundShipmentsResult result = inboundService
                .getListInboundShipmentsResponseByStatus(status)
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
        List<String> status = new ArrayList<String>() {{
            add("WORKING");
            add("SHIPPED");
            add("IN_TRANSIT");
            add("DELIVERED");
            add("CHECKED_IN");
            add("RECEIVING");
        }};
        inboundService
                .getListInboundShipmentsResponseByStatusWithLastDay(status)
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

    private void print(Object o) {
        try {
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
