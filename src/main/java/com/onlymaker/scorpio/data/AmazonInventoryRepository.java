package com.onlymaker.scorpio.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Date;

public interface AmazonInventoryRepository extends PagingAndSortingRepository<AmazonInventory, Long> {
    AmazonInventory findByMarketAndAsinAndCreateDate(String market, String asin, Date date);
    Page<AmazonInventory> findByCreateDateBefore(Date date, Pageable pageable);
    Page<AmazonInventory> findBySku(String sku, Pageable pageable);
}
