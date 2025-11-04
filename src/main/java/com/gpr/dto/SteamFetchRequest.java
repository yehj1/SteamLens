package com.gpr.dto;

import jakarta.validation.constraints.NotBlank;

public class SteamFetchRequest {

    @NotBlank
    private String appId;
    private String lang;
    private Integer limit;
    private String filter;
    private String reviewType;
    private String purchaseType;
    private Integer dayRange;
    private String appUuid;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getReviewType() {
        return reviewType;
    }

    public void setReviewType(String reviewType) {
        this.reviewType = reviewType;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public Integer getDayRange() {
        return dayRange;
    }

    public void setDayRange(Integer dayRange) {
        this.dayRange = dayRange;
    }

    public String getAppUuid() {
        return appUuid;
    }

    public void setAppUuid(String appUuid) {
        this.appUuid = appUuid;
    }
}
