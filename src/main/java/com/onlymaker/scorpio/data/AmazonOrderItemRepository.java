package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonOrderItemRepository extends CrudRepository<AmazonOrderItem, Long> {
    AmazonOrderItem findByAmazonOrderItemId(String amazonOrderItemId);
}
