package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonInventoryRepository extends CrudRepository<AmazonInventory, Long> {
    AmazonInventory findByAsinAndSellerSku(String asin, String sellerSku);
}
