package com.gpr.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gpr.config.GprProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.List;

@Component
public class VolcengineArkClient {

    private static final Logger log = LoggerFactory.getLogger(VolcengineArkClient.class);

    private final WebClient volcWebClient;
    private final GprProperties properties;
    private final ObjectMapper objectMapper;

    public VolcengineArkClient(@Qualifier("volcWebClient") WebClient volcWebClient,
                               GprProperties properties,
                               ObjectMapper objectMapper) {
        this.volcWebClient = volcWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String chat(String systemPrompt, List<String> userMessages) throws IOException {
        Assert.notEmpty(userMessages, "userMessages must not be empty");
        ensureConfigured();

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", properties.getVolc().getModel());
        payload.put("temperature", properties.getVolc().getTemperature());
        ArrayNode messages = payload.putArray("messages");
        ObjectNode systemNode = messages.addObject();
        systemNode.put("role", "system");
        systemNode.put("content", systemPrompt);
        for (String userMessage : userMessages) {
            ObjectNode userNode = messages.addObject();
            userNode.put("role", "user");
            userNode.put("content", userMessage);
        }

        String raw;
        try {
            raw = volcWebClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getVolc().getApiKey())
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Volcengine Ark error: {}", ex.getResponseBodyAsString());
            throw new IOException("Volcengine Ark call failed: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IOException("Volcengine Ark request failed", ex);
        }
        if (raw == null || raw.isBlank()) {
            throw new IOException("Volcengine Ark returned empty response");
        }
        JsonNode node = objectMapper.readTree(raw);
        JsonNode choices = node.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IOException("Volcengine Ark response missing choices");
        }
        JsonNode choice = choices.get(0);
        return choice.path("message").path("content").asText();
    }

    private void ensureConfigured() {
        if (properties.getVolc().getApiKey() == null || properties.getVolc().getApiKey().isBlank()) {
            throw new IllegalStateException("VOLC_API_KEY is not configured");
        }
    }
}
