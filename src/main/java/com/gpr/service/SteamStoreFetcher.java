package com.gpr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpr.config.GprProperties;
import com.gpr.domain.Review;
import com.gpr.domain.ReviewSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("steamFetcher")
public class SteamStoreFetcher implements ReviewFetcher {

    private static final Logger log = LoggerFactory.getLogger(SteamStoreFetcher.class);

    private final HttpClient httpClient;
    private final GprProperties.SteamProperties steamProperties;
    private final ObjectMapper objectMapper;

    public SteamStoreFetcher(ObjectMapper objectMapper, GprProperties properties) {
        this.objectMapper = objectMapper;
        this.steamProperties = properties.getSteam();
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(1, steamProperties.getConnectTimeoutSeconds())))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_1_1);
        if (steamProperties.getProxyHost() != null && !steamProperties.getProxyHost().isBlank()
                && steamProperties.getProxyPort() != null && steamProperties.getProxyPort() > 0) {
            builder.proxy(java.net.ProxySelector.of(new java.net.InetSocketAddress(
                    steamProperties.getProxyHost(), steamProperties.getProxyPort())));
        }
        this.httpClient = builder.build();
    }

    @Override
    public String sourceName() {
        return ReviewSource.STEAM.value();
    }

    @Override
    public List<Review> fetch(String packageId, String country, String lang, int limit) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public List<UnifiedReview> fetch(FetchRequest req) throws Exception {
        String appId = req.targetId();
        String lang = choose(req.lang(), "all");
        int limit = limit(req.limit(), 500);
        String filter = choose(req.filter(), "recent");
        String reviewType = choose(req.reviewType(), "all");
        String purchase = choose(req.purchaseType(), "all");
        Integer dayRange = req.dayRange();

        List<UnifiedReview> collected = new ArrayList<>();
        String cursor = "*";

        while (collected.size() < limit) {
            String response = executeRequest(appId, lang, filter, reviewType, purchase, dayRange, cursor);

            if (response == null || response.isBlank()) {
                break;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode reviewsNode = root.path("reviews");
            if (!reviewsNode.isArray() || reviewsNode.isEmpty()) {
                break;
            }

            cursor = root.path("cursor").asText(cursor);

            for (JsonNode node : reviewsNode) {
                UnifiedReview review = toUnifiedReview(appId, lang, node);
                collected.add(review);
                if (collected.size() >= limit) {
                    break;
                }
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.debug("Fetched {} steam reviews for app {}", collected.size(), appId);
        return collected;
    }

    private String executeRequest(String appId,
                                  String lang,
                                  String filter,
                                  String reviewType,
                                  String purchase,
                                  Integer dayRange,
                                  String cursor) throws IOException {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(buildUri(appId, lang, filter, reviewType, purchase, dayRange, cursor))
                        .GET()
                        .timeout(Duration.ofSeconds(Math.max(5, steamProperties.getRequestTimeoutSeconds())))
                        .header("User-Agent", steamProperties.getUserAgent())
                        .header("Accept", "application/json, text/javascript, */*; q=0.01")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .header("Referer", steamProperties.getBaseUrl() + "/app/" + appId)
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Cookie", steamProperties.getCookie())
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                }
                log.warn("Steam response status {} on attempt {}", response.statusCode(), attempt);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("Steam fetch interrupted", ex);
            } catch (IOException ex) {
                log.warn("Steam fetch attempt {} failed: {}", attempt, ex.getMessage());
                if (attempt >= 2) {
                    throw new IOException("Steam request failed after retries", ex);
                }
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Steam fetch interrupted", ie);
                }
            }
        }
        return null;
    }

    private URI buildUri(String appId,
                         String lang,
                         String filter,
                         String reviewType,
                         String purchase,
                         Integer dayRange,
                         String cursor) {
        String base = steamProperties.getBaseUrl() != null && !steamProperties.getBaseUrl().isBlank()
                ? steamProperties.getBaseUrl()
                : "https://store.steampowered.com";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        StringBuilder sb = new StringBuilder(base)
                .append("/appreviews/")
                .append(appId)
                .append("?json=1")
                .append("&num_per_page=100")
                .append("&filter=").append(encode(filter))
                .append("&review_type=").append(encode(reviewType))
                .append("&purchase_type=").append(encode(purchase))
                .append("&language=").append(encode(lang))
                .append("&cursor=").append(encodeCursor(cursor));
        if (dayRange != null && dayRange > 0) {
            sb.append("&day_range=").append(dayRange);
        }
        return URI.create(sb.toString());
    }

    private UnifiedReview toUnifiedReview(String appId, String lang, JsonNode node) throws Exception {
        String recId = text(node, "recommendationid");
        String content = text(node, "review");
        Instant created = epoch(node.path("timestamp_created"));

        JsonNode authorNode = node.path("author");
        String authorId = authorNode.path("steamid").asText(null);
        Integer playtime = authorNode.path("playtime_forever").isNumber()
                ? authorNode.path("playtime_forever").asInt()
                : null;

        String rawJson = objectMapper.writeValueAsString(node);

        return new UnifiedReview(recId, appId, sourceName(), lang, null, content, created, authorId, playtime, rawJson);
    }

    private static String choose(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int limit(Integer value, int fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return Math.min(value, 1000);
    }

    private static Instant epoch(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        return Instant.ofEpochSecond(node.asLong());
    }

    private static String text(JsonNode node, String field) {
        JsonNode child = node.path(field);
        return child.isMissingNode() || child.isNull() ? null : child.asText();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String encodeCursor(String value) {
        if (value == null || value.isBlank()) {
            return "*";
        }
        String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);
        // Steam expects literal '*' for the first page cursor
        return encoded.replace("%2A", "*");
    }
}
