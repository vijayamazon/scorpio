package com.onlymaker.scorpio.mws;

import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.*;
import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.config.Amazon;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class ReportServiceTest {
    private ReportService reportService;
    @Autowired
    Amazon amazon;

    @Before
    public void setup() {
        reportService = new ReportService(amazon.getList().get(0));
    }

    @Test
    public void requestReport() throws MarketplaceWebServiceException {
        ReportRequestInfo info = reportService.requestReport(ReportService.REPORT_TYPE.get("order")).getRequestReportResult().getReportRequestInfo();
        System.out.println("submit:" + info.getSubmittedDate());
        System.out.println("requestId:" + info.getReportRequestId());
        System.out.println("requestType:" + info.getReportType());
        System.out.println("reportId:" + info.getGeneratedReportId());
        System.out.println("status:" + info.getReportProcessingStatus());
    }

    @Test
    public void reportList() throws MarketplaceWebServiceException, InterruptedException {
        GetReportListResponse response = reportService.getReportList(ReportService.REPORT_TYPE.get("order"));
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
    public void getReport() throws MarketplaceWebServiceException, IOException {
        String id = "12104113200017844";
        File report = new File("/tmp/report");
        GetReportRequest request = reportService.prepareGetReport(id, new FileOutputStream(report));
        GetReportResponse response = reportService.getReport(request);
        System.out.println(response.getResponseHeaderMetadata());
        request.getReportOutputStream().close();
        System.out.println(new String(Files.readAllBytes(report.toPath())));
    }

    private void printReportInfo(List<ReportInfo> list) {
        list.forEach(report -> {
            System.out.println(report.getReportType());
            System.out.println(report.getReportId());
            System.out.println(report.getAvailableDate());
        });
    }
}
