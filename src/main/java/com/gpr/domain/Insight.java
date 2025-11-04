package com.gpr.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Insight {

    private UUID id;
    private UUID appId;
    private List<String> countries;
    private String lang;
    private Instant runAt;
    private String model;
    private String summaryMd;
    private String summaryJson;

    public Insight() {
    }

    public Insight(UUID id, UUID appId, List<String> countries, String lang, Instant runAt,
                   String model, String summaryMd, String summaryJson) {
        this.id = id;
        this.appId = appId;
        this.countries = countries;
        this.lang = lang;
        this.runAt = runAt;
        this.model = model;
        this.summaryMd = summaryMd;
        this.summaryJson = summaryJson;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAppId() {
        return appId;
    }

    public void setAppId(UUID appId) {
        this.appId = appId;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Instant getRunAt() {
        return runAt;
    }

    public void setRunAt(Instant runAt) {
        this.runAt = runAt;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSummaryMd() {
        return summaryMd;
    }

    public void setSummaryMd(String summaryMd) {
        this.summaryMd = summaryMd;
    }

    public String getSummaryJson() {
        return summaryJson;
    }

    public void setSummaryJson(String summaryJson) {
        this.summaryJson = summaryJson;
    }
}
