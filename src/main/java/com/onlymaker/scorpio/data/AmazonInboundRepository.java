package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonInboundRepository extends CrudRepository<AmazonInbound, Long> {
    AmazonInbound findByShipmentId(String shipmentId);
}
