package com.onlymaker.scorpio.api;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

@RestController
@RequestMapping(value = "/metrics", produces = TextFormat.CONTENT_TYPE_004)
public class Metrics {
    private CollectorRegistry collectorRegistry;

    public Metrics(@Autowired CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
    }

    @RequestMapping({"", "/"})
    private String collect() throws IOException {
        Writer writer = new StringWriter();
        TextFormat.write004(writer, collectorRegistry.metricFamilySamples());
        return writer.toString();
    }
}
