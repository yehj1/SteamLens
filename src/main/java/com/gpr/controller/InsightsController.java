package com.gpr.controller;

import com.gpr.dto.InsightResponse;
import com.gpr.dto.InsightSummarizeRequest;
import com.gpr.service.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/insights")
@Tag(name = "Insights", description = "Generate insights using Volcengine Ark")
public class InsightsController {

    private final InsightService insightService;

    public InsightsController(InsightService insightService) {
        this.insightService = insightService;
    }

    @PostMapping("/summarize")
    @Operation(summary = "Generate insights via Volcengine Ark LLM")
    public ResponseEntity<InsightResponse> summarize(@Valid @RequestBody InsightSummarizeRequest request) throws IOException {
        InsightResponse response = insightService.summarize(request);
        return ResponseEntity.ok(response);
    }
}
