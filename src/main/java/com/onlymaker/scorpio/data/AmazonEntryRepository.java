package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonEntryRepository extends CrudRepository<AmazonEntry, Long> {
    Iterable<AmazonEntry> findAllByStatusOrderByMarketAscAsin(int status);
}
