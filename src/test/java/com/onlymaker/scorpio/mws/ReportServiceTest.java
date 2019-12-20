package com.onlymaker.scorpio.mws;

import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.*;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
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

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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

    /*
     * requestId : 2ff2c572-e7f6-4bef-b326-0c2a1705f4ae
     * responseContext : RB9ThN1MPVjepZSij6tAw4pNJTRZ7kZbdHUX47UJGrNK39wVxED40OJGOleCYvMf2jnrX4cxd5c=
     * timestamp : 2019-05-19T11:56:03.527Z
     */
    @Test
    public void getReport() throws MarketplaceWebServiceException, IOException {
        String id = "17705881762018223";
        File report = new File("/tmp/report");
        GetReportRequest request = reportService.prepareGetReport(id, new FileOutputStream(report));
        GetReportResponse response = reportService.getReport(request);
        System.out.println(response.getResponseHeaderMetadata());
        request.getReportOutputStream().close();
        InputStream inputStream = new BufferedInputStream(new FileInputStream(report));
        CharsetDetector detector = new CharsetDetector();
        detector.setText(inputStream);
        CharsetMatch encoding = detector.detect();
        inputStream.close();
        if (encoding != null) {
            System.out.println("Detect encoding: " + encoding);
            Files.readAllLines(report.toPath(), Charset.forName(encoding.getName())).forEach(System.out::println);
        } else {
            System.out.println("Default encoding: " + Charset.defaultCharset());
            Files.readAllLines(report.toPath()).forEach(System.out::println);
        }
    }

    @Test
    public void splitReport() throws IOException {
        Files.readAllLines(Paths.get("/tmp/report")).forEach(line -> {
            if (!line.startsWith("sku")) {
                String[] elements = line.split("\t");
                Arrays.asList(elements).forEach(System.out::println);
            }
        });
    }

    @Test
    public void fbaReturn() throws MarketplaceWebServiceException {
        String line = "\"2019-12-18T07:07:33+00:00\",\"114-1560669-0185045\",\"ARUS-P90106A-US12-FBA\",\"B07PYVVL1W\",\"X002206XUN\",\"Onlymaker Women's Sexy Ankle Strap Open Toe Platform Stiletto Sandals Single Band High Heel Party Dress Shoes Black US12\",\"1\",\"SDF9\",\"SELLABLE\",\"APPAREL_TOO_SMALL\",\"Unit returned to inventory\",\"LPNRR868744056\",";
        if (line.endsWith(",")) {
            line += "\"\"";
        }
        String[] elements = line.substring(1, line.length() - 1).split("\",\"", -1);
        Arrays.asList(elements).forEach(e -> System.out.println(e));
        System.out.println(elements.length);
    }

    private void printReportInfo(List<ReportInfo> list) {
        list.forEach(report -> {
            System.out.println(report.getReportType());
            System.out.println(report.getReportId());
            System.out.println(report.getAvailableDate());
        });
    }
}
