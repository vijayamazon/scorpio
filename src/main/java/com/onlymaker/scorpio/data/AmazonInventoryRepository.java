package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

import java.sql.Date;

public interface AmazonInventoryRepository extends CrudRepository<AmazonInventory, Long> {
    AmazonInventory findByMarketAndFnSkuAndCreateDate(String market, String fnSku, Date date);
}
