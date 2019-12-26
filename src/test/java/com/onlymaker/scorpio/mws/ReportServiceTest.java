package com.onlymaker.scorpio.mws;

import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.*;
import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class ReportServiceTest {
    private ReportService reportService;
    private String reportType = ReportService.REPORT_TYPE.get("inventory");
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;

    @Before
    public void setup() {
        reportService = new ReportService(appInfo, amazon.getList().get(0));
    }

    /*
    * submit:2019-05-17T06:52:02Z
    * requestId:62349018033
    * requestType:_GET_FLAT_FILE_OPEN_LISTINGS_DATA_
    * reportId:null
    * status:_SUBMITTED_
    *
    * submit:2019-05-20T03:15:52Z
    * requestId:62362018036
    * requestType:_GET_AMAZON_FULFILLED_SHIPMENTS_DATA_
    * reportId:null
    * status:_SUBMITTED_
    *
    * submit:2019-05-20T08:28:53Z
    * requestId:62363018036
    * requestType:_GET_FBA_MYI_UNSUPPRESSED_INVENTORY_DATA_
    * reportId:null
    * status:_SUBMITTED_
    */
    @Test
    public void requestReport() throws MarketplaceWebServiceException {
        ReportRequestInfo info = reportService.requestReport(reportType).getRequestReportResult().getReportRequestInfo();
        System.out.println("submit:" + info.getSubmittedDate());
        System.out.println("requestId:" + info.getReportRequestId());
        System.out.println("requestType:" + info.getReportType());
        System.out.println("reportId:" + info.getGeneratedReportId());
        System.out.println("status:" + info.getReportProcessingStatus());
    }

    /*
     * 62349018033
     * 14895932402018033
     * _DONE_
     * 62138018004
     * 14496160544018004
     * _DONE_
     */
    @Test
    public void requestReportList() throws MarketplaceWebServiceException {
        GetReportRequestListResult result = reportService.getReportRequestList(reportType).getGetReportRequestListResult();
        result.getReportRequestInfoList().forEach(r -> {
            System.out.println(r.getReportRequestId());
            System.out.println(r.getGeneratedReportId());
            System.out.println(r.getReportProcessingStatus());
        });
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            GetReportRequestListByNextTokenResult nextResult = reportService.getReportRequestListByNextToken(nextToken).getGetReportRequestListByNextTokenResult();
            System.out.println(nextResult.getReportRequestInfoList().size() + " more results");
        }
    }

    /*
     * _GET_FLAT_FILE_OPEN_LISTINGS_DATA_
     * 14895932402018033
     * 2019-05-17T06:52:16Z
     * _GET_FLAT_FILE_OPEN_LISTINGS_DATA_
     * 14496160544018004
     * 2019-04-18T03:25:01Z
     */
    @Test
    public void reportList() throws MarketplaceWebServiceException, InterruptedException {
        GetReportListResponse response = reportService.getReportList(reportType);
        GetReportListResult result = response.getGetReportListResult();
        printReportInfo(result.getReportInfoList());
        String nextToken = result.getNextToken();
        while (StringUtils.isNotEmpty(nextToken)) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("next: " + nextToken);
            GetReportListByNextTokenResponse nextResponse = reportService.getReportListByNextToken(nextToken);
            GetReportListByNextTokenResult nextResult = nextResponse.getGetReportListByNextTokenResult();
            printReportInfo(nextResult.getReportInfoList());
            nextToken = nextResult.getNextToken();
        }
    }

    @Test
    public void getReport() throws Exception {
        String id = "18210001977018253";
        reportService.getReportContent(id, new File("/tmp/report")).forEach(line -> {
            String[] data = reportService.splitReportLine(line);
            for(String s : data) {
                System.out.print(s + " | ");
            }
            System.out.println();
        });
    }

    private void printReportInfo(List<ReportInfo> list) {
        list.forEach(report -> {
            System.out.println(report.getReportType());
            System.out.println(report.getReportId());
            System.out.println(report.getAvailableDate());
        });
    }
}
