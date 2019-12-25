package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonProductRepository extends CrudRepository<AmazonProduct, Long> {
    AmazonProduct findByMarketAndAsin(String market, String asin);
    Iterable<AmazonProduct> findBySkuOrSkuIsNull(String sku);
}
