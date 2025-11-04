package com.gpr.domain;

import java.time.Instant;
import java.util.UUID;

public class Review {

    private UUID id;
    private UUID appId;
    private String reviewSource;
    private String externalId;
    private String country;
    private String lang;
    private Integer rating;
    private String author;
    private String content;
    private Instant reviewedAt;
    private String extraJson;
    private String recommendationId;
    private Integer playtimeForever;
    private String authorId;

    public Review() {
    }

    public Review(UUID id, UUID appId, String reviewSource, String externalId, String country, String lang,
                  Integer rating, String author, String content, Instant reviewedAt, String extraJson) {
        this(id, appId, reviewSource, externalId, country, lang, rating, author, content, reviewedAt, extraJson,
                null, null, null);
    }

    public Review(UUID id, UUID appId, String reviewSource, String externalId, String country, String lang,
                  Integer rating, String author, String content, Instant reviewedAt, String extraJson,
                  String recommendationId, Integer playtimeForever, String authorId) {
        this.id = id;
        this.appId = appId;
        this.reviewSource = reviewSource;
        this.externalId = externalId;
        this.country = country;
        this.lang = lang;
        this.rating = rating;
        this.author = author;
        this.content = content;
        this.reviewedAt = reviewedAt;
        this.extraJson = extraJson;
        this.recommendationId = recommendationId;
        this.playtimeForever = playtimeForever;
        this.authorId = authorId;
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

    public String getReviewSource() {
        return reviewSource;
    }

    public void setReviewSource(String reviewSource) {
        this.reviewSource = reviewSource;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getExtraJson() {
        return extraJson;
    }

    public void setExtraJson(String extraJson) {
        this.extraJson = extraJson;
    }

    public String getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(String recommendationId) {
        this.recommendationId = recommendationId;
    }

    public Integer getPlaytimeForever() {
        return playtimeForever;
    }

    public void setPlaytimeForever(Integer playtimeForever) {
        this.playtimeForever = playtimeForever;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
}
