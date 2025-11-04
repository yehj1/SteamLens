package com.gpr.dto;

import java.time.Instant;
import java.util.UUID;

public class ReviewResponse {
    private UUID id;
    private String packageId;
    private String reviewSource;
    private String country;
    private String lang;
    private Integer rating;
    private String author;
    private String content;
    private Instant reviewedAt;

    public ReviewResponse(UUID id, String packageId, String reviewSource, String country, String lang,
                          Integer rating, String author, String content, Instant reviewedAt) {
        this.id = id;
        this.packageId = packageId;
        this.reviewSource = reviewSource;
        this.country = country;
        this.lang = lang;
        this.rating = rating;
        this.author = author;
        this.content = content;
        this.reviewedAt = reviewedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getPackageId() {
        return packageId;
    }

    public String getReviewSource() {
        return reviewSource;
    }

    public String getCountry() {
        return country;
    }

    public String getLang() {
        return lang;
    }

    public Integer getRating() {
        return rating;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
