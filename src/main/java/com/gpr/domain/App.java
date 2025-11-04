package com.gpr.domain;

import java.time.Instant;
import java.util.UUID;

public class App {

    private UUID id;
    private String packageId;
    private String name;
    private Instant createdAt;

    public App() {
    }

    public App(UUID id, String packageId, String name, Instant createdAt) {
        this.id = id;
        this.packageId = packageId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
