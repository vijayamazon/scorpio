package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AmazonOrderItemRepository extends CrudRepository<AmazonOrderItem, Long> {
    AmazonOrderItem findByAmazonOrderItemId(String amazonOrderItemId);
    List<AmazonOrderItem> findAllByAmazonOrderId(String amazonOrderId);
}
