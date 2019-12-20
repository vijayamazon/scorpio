package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;

public interface AmazonFBAReturnRepository extends CrudRepository<AmazonFBAReturn, Long> {
    AmazonFBAReturn findOneByMarketAndTimeAndOrderIdAndAsin(String market, Timestamp time, String orderId, String asin);
}
