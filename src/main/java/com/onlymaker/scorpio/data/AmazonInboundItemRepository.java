package com.onlymaker.scorpio.data;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface AmazonInboundItemRepository extends PagingAndSortingRepository<AmazonInboundItem, Long> {
    Iterable<AmazonInboundItem> findAllByShipmentId(String shipmentId);
}
