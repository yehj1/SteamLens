package com.gpr.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class FetchRequest {

    @NotBlank
    private String packageId;
    private List<String> countries;
    private String lang;
    @Min(1)
    private Integer limitPerCountry;

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

    public Integer getLimitPerCountry() {
        return limitPerCountry;
    }

    public void setLimitPerCountry(Integer limitPerCountry) {
        this.limitPerCountry = limitPerCountry;
    }
}
