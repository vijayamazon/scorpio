package com.onlymaker.scorpio.mws;

import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.data.AmazonEntry;
import com.onlymaker.scorpio.data.AmazonEntryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class HtmlPageServiceTest {
    @Autowired
    HtmlPageService htmlPageService;
    @Autowired
    AmazonEntryRepository amazonEntryRepository;

    @Test
    public void fetch() {
        amazonEntryRepository.findAllByStatusOrderByAsin(AmazonEntry.STATUS_ENABLED).forEach(htmlPageService::parse);
    }
}
