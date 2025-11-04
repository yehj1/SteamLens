package com.gpr.dto;

import java.util.Map;

public class FetchResponse {

    private String packageId;
    private Map<String, Integer> insertedPerCountry;
    private int totalInserted;

    public FetchResponse(String packageId, Map<String, Integer> insertedPerCountry, int totalInserted) {
        this.packageId = packageId;
        this.insertedPerCountry = insertedPerCountry;
        this.totalInserted = totalInserted;
    }

    public String getPackageId() {
        return packageId;
    }

    public Map<String, Integer> getInsertedPerCountry() {
        return insertedPerCountry;
    }

    public int getTotalInserted() {
        return totalInserted;
    }
}
