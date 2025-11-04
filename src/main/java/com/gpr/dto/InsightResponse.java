package com.gpr.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class InsightResponse {
    private UUID id;
    private String packageId;
    private List<String> countries;
    private String lang;
    private Instant runAt;
    private String model;
    private String summaryMarkdown;
    private String summaryJson;

    public InsightResponse(UUID id, String packageId, List<String> countries, String lang, Instant runAt,
                           String model, String summaryMarkdown, String summaryJson) {
        this.id = id;
        this.packageId = packageId;
        this.countries = countries;
        this.lang = lang;
        this.runAt = runAt;
        this.model = model;
        this.summaryMarkdown = summaryMarkdown;
        this.summaryJson = summaryJson;
    }

    public UUID getId() {
        return id;
    }

    public String getPackageId() {
        return packageId;
    }

    public List<String> getCountries() {
        return countries;
    }

    public String getLang() {
        return lang;
    }

    public Instant getRunAt() {
        return runAt;
    }

    public String getModel() {
        return model;
    }

    public String getSummaryMarkdown() {
        return summaryMarkdown;
    }

    public String getSummaryJson() {
        return summaryJson;
    }
}
