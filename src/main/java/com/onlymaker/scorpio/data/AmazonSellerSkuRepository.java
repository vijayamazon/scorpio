package com.onlymaker.scorpio.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AmazonSellerSkuRepository extends PagingAndSortingRepository<AmazonSellerSku, Long> {
    Page<AmazonSellerSku> findByMarketAndSku(String market, String sku, Pageable pageable);
}
