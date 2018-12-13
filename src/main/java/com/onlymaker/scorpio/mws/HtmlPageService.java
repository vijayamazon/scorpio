package com.onlymaker.scorpio.mws;

import com.onlymaker.scorpio.data.AmazonEntry;
import com.onlymaker.scorpio.data.AmazonEntrySnapshot;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HtmlPageService {
    private final static Logger LOGGER = LoggerFactory.getLogger(HtmlPageService.class);
    private final static String chromeUserAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36";

    public AmazonEntrySnapshot parse(AmazonEntry entry) {
        AmazonEntrySnapshot snapshot = new AmazonEntrySnapshot();
        snapshot.setMarket(entry.getMarket());
        snapshot.setStore(entry.getStore());
        snapshot.setAsin(entry.getAsin());
        snapshot.setSku(entry.getSku());
        String url = entry.getUrl() + entry.getAsin();
        LOGGER.info("parse {}", url);
        try {
            HttpConnection connection = (HttpConnection) Jsoup.connect(url);
            Document document = connection.validateTLSCertificates(false).userAgent(chromeUserAgent).get();
            String rank = document.select("#SalesRank").text();
            LOGGER.info("rank: {}", rank);
            snapshot.setRankBest(matchRankBest(rank));
            String review = document.select("[data-hook=total-review-count]").text();
            LOGGER.info("review: {}", review);
            snapshot.setReviewCount(matchReviewCount(review));
            Elements star = document.select(".a-meter");
            LOGGER.info("star: {}", star);
            snapshot.setStar5(getStarByRate(star, 5));
            snapshot.setStar4(getStarByRate(star, 4));
            snapshot.setStar3(getStarByRate(star, 3));
            snapshot.setStar2(getStarByRate(star, 2));
            snapshot.setStar1(getStarByRate(star, 1));
            int variable = document.select("#variation_color_name ul li").size();
            if (variable == 0) {
                variable = 1;
            }
            LOGGER.info("variable: {}", variable);
            snapshot.setVariable(variable);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getStarByRate(Elements elements, int rate) {
        Elements star  = elements.select(String.format(".%dstar", rate));
        if (star.size() != 0) {
            return Integer.valueOf(star.get(0).attr("aria-label").trim().replace("%", ""));
        }
        return 0;
    }

    private int matchRankBest(String rank) {
        Matcher matcher = Pattern.compile("(#\\d+(,?\\d*)*\\s)").matcher(rank);
        int best = 0;
        while (matcher.find()) {
            String rankString = rank.substring(matcher.start(), matcher.end()).replaceAll("[#,\\s]", "");
            int rankInt = Integer.valueOf(rankString);
            if (best == 0 || best > rankInt) {
                best = rankInt;
            }
        }
        return best;
    }

    private int matchReviewCount(String review) {
        Matcher matcher = Pattern.compile("(\\d+\\s)").matcher(review);
        if (matcher.find()) {
            String rc = review.substring(matcher.start(), matcher.end()).trim();
            return Integer.valueOf(rc);
        }
        return 0;
    }
}
