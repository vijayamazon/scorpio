package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonOrderRepository extends CrudRepository<AmazonOrder, Long> {
    AmazonOrder findByAmazonOrderId(String amazonOrderId);
}
