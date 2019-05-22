package com.onlymaker.scorpio.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.sql.Date;

public interface AmazonInventoryRepository extends CrudRepository<AmazonInventory, Long> {
    AmazonInventory findByMarketAndFnSkuAndCreateDate(String market, String fnSku, Date date);
    Page<AmazonInventory> findByCreateDateBefore(Date date, Pageable pageable);
}
