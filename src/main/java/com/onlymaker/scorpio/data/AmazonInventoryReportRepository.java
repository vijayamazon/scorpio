package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonInventoryReportRepository extends CrudRepository<AmazonInventoryReport, Long> {
    AmazonInventoryReport findOneBySellerSku(String sellerSku);
}
