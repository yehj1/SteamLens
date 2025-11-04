package com.gpr.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class InsightSummarizeRequest {

    @NotBlank
    private String packageId;
    private List<String> countries;
    private String lang;
    @Min(1)
    private Integer maxRows;

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
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

    public Integer getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }
}
