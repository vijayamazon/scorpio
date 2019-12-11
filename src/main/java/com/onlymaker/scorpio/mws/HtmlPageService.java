package com.onlymaker.scorpio.mws;

import com.onlymaker.scorpio.data.AmazonAsin;
import com.onlymaker.scorpio.data.AmazonAsinRepository;
import com.onlymaker.scorpio.data.AmazonEntry;
import com.onlymaker.scorpio.data.AmazonEntrySnapshot;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HtmlPageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlPageService.class);
    private static final String CHROME_UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36";

    @Autowired
    AmazonAsinRepository amazonAsinRepository;

    public AmazonEntrySnapshot parse(AmazonEntry entry) throws IOException, InterruptedException {
        AmazonEntrySnapshot snapshot = new AmazonEntrySnapshot();
        snapshot.setMarket(entry.getMarket());
        snapshot.setAsin(entry.getAsin());
        snapshot.setCreateDate(new Date(System.currentTimeMillis()));
        String url = entry.getUrl() + entry.getAsin();
        LOGGER.info("fetch: {}", url);
        Thread.sleep((long)(Math.random() * 10000));
        HttpConnection connection = (HttpConnection) Jsoup.connect(url);
        Document document = connection.userAgent(CHROME_UA).get();
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

        Elements colorVariable = document.select("#variation_color_name ul li");
        int variable = colorVariable.size();
        if (variable == 0) {
            variable = 1;
            colorVariable = document.select("#variation_color_name .selection");
            if (colorVariable.size() == 1) {
                try {
                    parseAsin(entry.getMarket(), entry.getAsin(), colorVariable.get(0).text(), document);
                } catch (Throwable t) {
                    LOGGER.error("parse asin error {}", t.getMessage(), t);
                }
            }
        } else {
            for (int i = 0; i < variable; i++) {
                Element color = colorVariable.get(i);
                try {
                    if (i > 0) {
                        String asin = color.attr("data-defaultasin");
                        LOGGER.info("asin: " + entry.getUrl() + asin);
                        Thread.sleep((long)(Math.random() * 10000));
                        connection = (HttpConnection) Jsoup.connect(entry.getUrl() + asin);
                        document = connection.userAgent(CHROME_UA).get();
                    }
                    parseAsin(entry.getMarket(), entry.getAsin(), color.select("img").get(0).attr("alt"), document);
                } catch (Throwable t) {
                    LOGGER.error("parse asin error {}", t.getMessage(), t);
                }
            }
        }
        LOGGER.info("variable: {}", variable);
        snapshot.setVariable(variable);

        return snapshot;
    }

    private int getStarByRate(Elements elements, int rate) {
        Elements star = elements.select(String.format(".%dstar", rate));
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

    private void parseAsin(String market, String parent, String color, Document document) {
        // size
        Elements sizeVariable = document.select("#variation_size_name option");
        // ignore first option
        for (int i = 1; i < sizeVariable.size(); i++) {
            if (sizeVariable.hasClass("dropdownAvailable")) {
                Element size = sizeVariable.get(i);
                String value = size.val();
                if (value != null) {
                    String[] parts = value.split(",");
                    if (parts.length == 2) {
                        String asin = parts[1];
                        String variable = size.text();
                        saveOrUpdateAsin(market, parent, asin, color, variable);
                    }
                }
            }
        }
    }

    private void saveOrUpdateAsin(String market, String parent, String asin, String color, String size) {
        Date date = new Date(System.currentTimeMillis());
        AmazonAsin amazonAsin = amazonAsinRepository.findByMarketAndAsin(market, asin);
        if (amazonAsin == null) {
            amazonAsin = new AmazonAsin();
            amazonAsin.setMarket(market);
            amazonAsin.setParentAsin(parent);
            amazonAsin.setAsin(asin);
            amazonAsin.setCreateDate(date);
        }
        amazonAsin.setColor(color);
        amazonAsin.setSize(size);
        amazonAsin.setUpdateDate(date);
        amazonAsinRepository.save(amazonAsin);
    }
}
