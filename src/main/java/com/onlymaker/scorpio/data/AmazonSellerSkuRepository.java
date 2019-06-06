package com.onlymaker.scorpio.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AmazonSellerSkuRepository extends PagingAndSortingRepository<AmazonSellerSku, Long> {
    AmazonSellerSku findByMarketAndSellerSku(String market, String sellerSku);
    Page<AmazonSellerSku> findAll(Pageable pageable);
}
