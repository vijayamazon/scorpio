package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonAsinRepository extends CrudRepository<AmazonAsin, Long> {
    AmazonAsin findByMarketAndAsinAndColorAndSize(String market, String asin, String color, String size);
}
