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
import java.sql.Timestamp;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HtmlPageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlPageService.class);
    private static final String CHROME_UA = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36";

    public AmazonEntrySnapshot parse(AmazonEntry entry) {
        AmazonEntrySnapshot snapshot = new AmazonEntrySnapshot();
        snapshot.setMarket(entry.getMarket());
        snapshot.setStore(entry.getStore());
        snapshot.setAsin(entry.getAsin());
        snapshot.setSku(entry.getSku());
        snapshot.setCreateTime(new Timestamp(System.currentTimeMillis()));
        String url = entry.getUrl() + entry.getAsin();
        LOGGER.info("parse {}", url);
        try {
            HttpConnection connection = (HttpConnection) Jsoup.connect(url);
            Document document = connection.validateTLSCertificates(false).userAgent(CHROME_UA).get();
            String rank = document.select("#SalesRank").text();
            LOGGER.info("rank: {}", rank);
            snapshot.setRankBest(matchRankBest(rank, snapshot.getMarket()));
            String review = document.select("[data-hook=total-review-count]").text();
            LOGGER.info("review: {}", review);
            snapshot.setReviewCount(matchReviewCount(review));
            String average = document.select("[data-hook=rating-out-of-text]").text();
            LOGGER.info("average: {}", average);
            snapshot.setStarAverage(matchStarAverage(average));
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
        return snapshot;
    }

    private int getStarByRate(Elements elements, int rate) {
        Elements star  = elements.select(String.format(".%dstar", rate));
        if (star.size() != 0) {
            return Integer.valueOf(star.get(0).attr("aria-label").trim().replace("%", ""));
        }
        return 0;
    }

    private int matchRankBest(String rank, String market) {
        String regex = Objects.equals(market, "DE") ? "(Nr\\.\\s\\d+(\\.?\\d*)*\\s)" : "(#\\d+(,?\\d*)*\\s)";
        Matcher matcher = Pattern.compile(regex).matcher(rank);
        int best = 0;
        while (matcher.find()) {
            String rankString = rank.substring(matcher.start(), matcher.end()).replaceAll("[^0-9]", "");
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

    private float matchStarAverage(String review) {
        review = review.replace(",", ".");
        Matcher matcher = Pattern.compile("(\\d+(\\.?\\d*)*\\s)").matcher(review);
        if (matcher.find()) {
            String as = review.substring(matcher.start(), matcher.end()).trim();
            return Float.parseFloat(as);
        }
        return 0f;
    }
}
