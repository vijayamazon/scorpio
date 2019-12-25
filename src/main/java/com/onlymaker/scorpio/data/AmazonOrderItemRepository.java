package com.onlymaker.scorpio.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Timestamp;

public interface AmazonOrderItemRepository extends PagingAndSortingRepository<AmazonOrderItem, Long> {
    AmazonOrderItem findByAmazonOrderItemId(String amazonOrderItemId);
    Page<AmazonOrderItem> findByCreateTimeBefore(Timestamp timestamp, Pageable pageable);
    Page<AmazonOrderItem> findBySku(String sku, Pageable pageable);
    AmazonOrderItem findTopByAsinOrderByPurchaseDateDesc(String asin);
}
