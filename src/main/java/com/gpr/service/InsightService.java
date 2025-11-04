package com.gpr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpr.config.GprProperties;
import com.gpr.domain.App;
import com.gpr.domain.Insight;
import com.gpr.domain.Review;
import com.gpr.dto.InsightResponse;
import com.gpr.dto.InsightSummarizeRequest;
import com.gpr.llm.VolcengineArkClient;
import com.gpr.repo.AppRepository;
import com.gpr.repo.InsightRepository;
import com.gpr.repo.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class InsightService {

    private static final int DEFAULT_MAX_ROWS = 500;
    private static final int BATCH_SIZE = 50;
    private static final String SYSTEM_PROMPT = "你是产品研究助理。基于给定用户评论生成结构化洞察。" +
            "\n返回 JSON 字段：themes[{name, count, neg_ratio, sample_quotes<=3, related_needs<=5}], overall{pos,neg,neu}, key_findings[<=5]。" +
            "\n随后输出一段 Markdown 摘要（面向 PM/研发）。" +
            "\n严禁编造不存在的信息；仅依据评论文本。";

    private final ReviewRepository reviewRepository;
    private final InsightRepository insightRepository;
    private final AppRepository appRepository;
    private final VolcengineArkClient arkClient;
    private final ObjectMapper objectMapper;
    private final GprProperties properties;

    public InsightService(ReviewRepository reviewRepository,
                          InsightRepository insightRepository,
                          AppRepository appRepository,
                          VolcengineArkClient arkClient,
                          ObjectMapper objectMapper,
                          GprProperties properties) {
        this.reviewRepository = reviewRepository;
        this.insightRepository = insightRepository;
        this.appRepository = appRepository;
        this.arkClient = arkClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Transactional
    public InsightResponse summarize(InsightSummarizeRequest request) throws IOException {
        App app = appRepository.findByPackageId(request.getPackageId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown packageId: " + request.getPackageId()));

        List<String> countries = resolveCountries(request.getCountries());
        int maxRows = request.getMaxRows() != null ? request.getMaxRows() : DEFAULT_MAX_ROWS;
        List<Review> reviews = reviewRepository.findRecentByApp(app.getId(), countries, maxRows);
        if (reviews.isEmpty()) {
            throw new IllegalArgumentException("No reviews found for the requested filters");
        }

        String lang = request.getLang() != null ? request.getLang() : properties.getSerpapi().getDefaultLang();
        String header = String.format(Locale.ROOT,
                "以下为来自 %s 的 Google Play 评论样本（上限 %d，已去重）。请先输出 JSON，再输出 Markdown：",
                String.join(",", countries),
                Math.min(maxRows, reviews.size()));
        List<String> userMessages = buildUserMessages(header, reviews);
        String content = arkClient.chat(SYSTEM_PROMPT, userMessages);

        SummaryParts parts = splitSummary(content);
        Insight insight = new Insight(UUID.randomUUID(), app.getId(), countries, lang, Instant.now(),
                properties.getVolc().getModel(), parts.markdown(), parts.json());
        insightRepository.save(insight);
        return new InsightResponse(insight.getId(), app.getPackageId(), countries, lang, insight.getRunAt(),
                insight.getModel(), insight.getSummaryMd(), insight.getSummaryJson());
    }

    private List<String> resolveCountries(List<String> countries) {
        if (!CollectionUtils.isEmpty(countries)) {
            return countries;
        }
        String defaults = properties.getSerpapi().getDefaultCountries();
        if (defaults == null || defaults.isBlank()) {
            return List.of("us");
        }
        String[] split = defaults.split(",");
        List<String> resolved = new ArrayList<>();
        for (String token : split) {
            if (!token.isBlank()) {
                resolved.add(token.trim().toLowerCase(Locale.ROOT));
            }
        }
        return resolved.isEmpty() ? List.of("us") : resolved;
    }

    private List<String> buildUserMessages(String header, List<Review> reviews) {
        List<String> messages = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int count = 0;
        for (Review review : reviews) {
            if (count == 0) {
                current.append(header).append("\n");
            }
            count++;
            current.append(String.format(Locale.ROOT,
                    "<review>#%d %s/5 %s - \"%s\"</review>\n",
                    count,
                    review.getRating() != null ? review.getRating() : "?",
                    review.getReviewedAt() != null ? review.getReviewedAt() : "unknown",
                    sanitize(review.getContent())));
            if (count % BATCH_SIZE == 0) {
                messages.add(current.toString());
                current = new StringBuilder();
            }
        }
        if (current.length() > 0) {
            messages.add(current.toString());
        }
        if (messages.isEmpty()) {
            messages.add(header);
        }
        return messages;
    }

    private SummaryParts splitSummary(String content) throws IOException {
        if (content == null || content.isBlank()) {
            throw new IOException("LLM response is empty");
        }
        String trimmed = content.trim();
        int jsonStart = trimmed.indexOf('{');
        if (jsonStart < 0) {
            throw new IOException("Expected JSON object in LLM response");
        }
        String prefix = trimmed.substring(0, jsonStart).trim();
        String jsonCandidate = trimmed.substring(jsonStart);
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        int jsonEnd = -1;
        for (int i = 0; i < jsonCandidate.length(); i++) {
            char c = jsonCandidate.charAt(i);
            if (inString) {
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                } else if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        jsonEnd = i;
                        break;
                    }
                }
            }
        }
        if (jsonEnd == -1) {
            throw new IOException("Failed to locate JSON block in LLM response");
        }
        String jsonPart = jsonCandidate.substring(0, jsonEnd + 1).trim();
        JsonNode jsonNode = objectMapper.readTree(jsonPart);
        String markdownPart = jsonCandidate.substring(jsonEnd + 1).trim();
        if (!prefix.isEmpty()) {
            markdownPart = (markdownPart.isEmpty() ? prefix : prefix + "\n\n" + markdownPart).trim();
        }
        markdownPart = stripCodeFence(markdownPart);
        return new SummaryParts(objectMapper.writeValueAsString(jsonNode), markdownPart);
    }

    private String sanitize(String content) {
        if (content == null) {
            return "";
        }
        return content.replace("\"", "'");
    }

    private String stripCodeFence(String markdown) {
        if (markdown == null) {
            return null;
        }
        String trimmed = markdown.trim();
        if (trimmed.startsWith("```")) {
            int firstLineBreak = trimmed.indexOf('\n');
            int secondFence = trimmed.indexOf("```", 3);
            if (firstLineBreak > 0 && secondFence > firstLineBreak) {
                String inside = trimmed.substring(firstLineBreak + 1, secondFence);
                String rest = trimmed.substring(secondFence + 3).trim();
                return rest.isEmpty() ? inside.trim() : (inside + "\n\n" + rest).trim();
            }
        }
        return trimmed;
    }

    private record SummaryParts(String json, String markdown) {
        public String json() {
            return json;
        }

        public String markdown() {
            return markdown;
        }
    }
}
