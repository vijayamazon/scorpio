package com.onlymaker.scorpio.data;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface AmazonAgeRepository extends PagingAndSortingRepository<AmazonAge, Long> {
    AmazonAge findByMarketAndAsin(String market, String asin);
}
