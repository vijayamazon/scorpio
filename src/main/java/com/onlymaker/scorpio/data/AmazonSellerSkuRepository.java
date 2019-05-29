package com.onlymaker.scorpio.data;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface AmazonSellerSkuRepository extends PagingAndSortingRepository<AmazonSellerSku, Long> {
    AmazonSellerSku findByMarketAndSellerSku(String market, String sku);
}
