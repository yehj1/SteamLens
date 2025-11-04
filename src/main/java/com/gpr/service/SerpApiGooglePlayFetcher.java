package com.gpr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpr.config.GprProperties;
import com.gpr.domain.Review;
import com.gpr.domain.ReviewSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component("googlePlayFetcher")
public class SerpApiGooglePlayFetcher implements ReviewFetcher {

    private static final Logger log = LoggerFactory.getLogger(SerpApiGooglePlayFetcher.class);
    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)
    };

    private final WebClient serpApiWebClient;
    private final ObjectMapper objectMapper;
    private final GprProperties properties;

    public SerpApiGooglePlayFetcher(@Qualifier("serpApiWebClient") WebClient serpApiWebClient,
                                    ObjectMapper objectMapper,
                                    GprProperties properties) {
        this.serpApiWebClient = serpApiWebClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public List<Review> fetch(String packageId, String country, String lang, int limit) throws IOException {
        ensureConfigured();
        List<Review> collected = new ArrayList<>();
        String nextPageToken = null;
        while (collected.size() < limit) {
            JsonNode response = executeWithRetry(packageId, country, lang, nextPageToken);
            if (response == null || response.isEmpty()) {
                break;
            }
            JsonNode errorNode = response.get("error");
            if (errorNode != null && !errorNode.isNull()) {
                throw new IOException("SerpApi error: " + errorNode.asText());
            }
            JsonNode reviews = response.path("reviews");
            if (!reviews.isArray() || reviews.isEmpty()) {
                break;
            }
            for (JsonNode reviewNode : reviews) {
                Review review = toReview(reviewNode, country, lang);
                collected.add(review);
                if (collected.size() >= limit) {
                    break;
                }
            }
            JsonNode pagination = response.path("serpapi_pagination");
            String token = pagination.path("next_page_token").asText(null);
            if (token == null || token.isBlank()) {
                break;
            }
            nextPageToken = token;
        }
        return collected;
    }

    @Override
    public String sourceName() {
        return ReviewSource.GOOGLE_PLAY.value();
    }

    private void ensureConfigured() {
        if (properties.getSerpapi().getApiKey() == null || properties.getSerpapi().getApiKey().isBlank()) {
            throw new IllegalStateException("SERPAPI_KEY is not configured");
        }
    }

    private JsonNode executeWithRetry(String packageId, String country, String lang, String pageToken) throws IOException {
        int retries = Math.max(0, properties.getSerpapi().getMaxRetries());
        IOException lastError = null;
        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                String response = serpApiWebClient
                        .get()
                        .uri(builder -> builder
                                .path("/search.json")
                                .queryParam("engine", properties.getSerpapi().getEngine())
                                .queryParam("store", "apps")
                                .queryParam("product_id", packageId)
                                .queryParam("gl", country)
                                .queryParam("hl", lang)
                                .queryParam("all_reviews", true)
                                .queryParam("num", 100)
                                .queryParam("api_key", properties.getSerpapi().getApiKey())
                                .queryParamIfPresent("page_token", pageToken == null || pageToken.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(pageToken))
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                if (response == null || response.isBlank()) {
                    return objectMapper.createObjectNode();
                }
                return objectMapper.readTree(response);
            } catch (WebClientResponseException ex) {
                if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && attempt < retries) {
                    log.warn("SerpApi rate limited, retrying attempt {}/{}", attempt + 1, retries + 1);
                    continue;
                }
                lastError = new IOException("SerpApi call failed: " + ex.getMessage(), ex);
            } catch (WebClientRequestException ex) {
                log.warn("SerpApi request error: {}", ex.getMessage());
                lastError = new IOException("SerpApi network failure", ex);
            } catch (Exception ex) {
                lastError = new IOException("SerpApi parsing failure", ex);
            }
        }
        if (lastError != null) {
            throw lastError;
        }
        return objectMapper.createObjectNode();
    }

    private Review toReview(JsonNode node, String country, String lang) {
        String externalId = node.path("id").asText(UUID.randomUUID().toString());
        UUID id = UUID.nameUUIDFromBytes((sourceName() + "|" + externalId).getBytes(StandardCharsets.UTF_8));
        Integer rating = node.hasNonNull("rating") ? node.get("rating").asInt() : null;
        String author = node.path("userName").asText(null);
        String content = node.path("snippet").asText(null);
        if (content == null || content.isBlank()) {
            content = node.path("content").asText(null);
        }
        if (content == null) {
            content = "";
        }
        Instant reviewedAt = parseDate(node.path("date").asText(null));
        String extra;
        try {
            extra = objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            extra = null;
        }
        return new Review(id, null, sourceName(), externalId, country, lang, rating, author, content, reviewedAt, extra);
    }

    private Instant parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                if (formatter == DateTimeFormatter.ISO_DATE_TIME) {
                    return OffsetDateTime.parse(raw, formatter).toInstant();
                }
                if (formatter == DateTimeFormatter.ISO_DATE) {
                    return LocalDate.parse(raw, formatter).atStartOfDay().toInstant(ZoneOffset.UTC);
                }
                LocalDate parsed = LocalDate.parse(raw, formatter);
                return parsed.atStartOfDay().toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return OffsetDateTime.parse(raw).toInstant();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(raw).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
