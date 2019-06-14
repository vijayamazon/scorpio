package com.onlymaker.scorpio.mws;

import com.onlymaker.scorpio.Main;
import com.onlymaker.scorpio.data.AmazonEntry;
import com.onlymaker.scorpio.data.AmazonEntryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class HtmlPageServiceTest {
    @Autowired
    HtmlPageService htmlPageService;
    @Autowired
    AmazonEntryRepository amazonEntryRepository;

    @Test
    public void fetch() throws IOException {
        Iterable<AmazonEntry> i = amazonEntryRepository.findByStatusOrderByMarketAscAsin(AmazonEntry.STATUS_ENABLED);
        if (i.iterator().hasNext()) {
            AmazonEntry entry = i.iterator().next();
            htmlPageService.parse(entry);
        }
    }
}
