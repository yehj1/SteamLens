package com.gpr.service;

import com.gpr.domain.Review;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface ReviewFetcher {
    List<Review> fetch(String packageId, String country, String lang, int limit) throws IOException;

    default List<UnifiedReview> fetch(FetchRequest req) throws Exception {
        throw new UnsupportedOperationException("Unified fetch not implemented for source: " + sourceName());
    }

    String sourceName();

    record FetchRequest(String targetId, String lang, Integer limit,
                        String filter, String reviewType, String purchaseType, Integer dayRange) {}

    record UnifiedReview(String externalId, String targetId, String source, String lang,
                         Integer rating, String content, Instant createdAt,
                         String authorId, Integer playtimeForever, String rawJson) {}
}
