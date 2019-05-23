package com.onlymaker.scorpio.data;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface AmazonSellerSkuRepository extends PagingAndSortingRepository<AmazonSellerSku, Long> {
    AmazonSellerSku findByMarketAndSku(String market, String sku);
}
