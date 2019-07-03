package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonAsinRepository extends CrudRepository<AmazonAsin, Long> {
    AmazonAsin findByMarketAndAsin(String market, String asin);
}
