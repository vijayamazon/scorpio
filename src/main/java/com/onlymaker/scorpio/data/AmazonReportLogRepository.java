package com.onlymaker.scorpio.data;

import org.springframework.data.repository.CrudRepository;

public interface AmazonReportLogRepository extends CrudRepository<AmazonReportLog, Long> {
    AmazonReportLog findOneByRequestIdAndStatus(String requestId, int status);
}
