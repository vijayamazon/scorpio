package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonInboundItemRepository extends CrudRepository<AmazonInboundItem, Long> {
    Iterable<AmazonInboundItem> findAllByShipmentId(String shipmentId);
}
