package com.gpr.controller;

import com.gpr.dto.ReviewResponse;
import com.gpr.service.ReviewFetchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@Validated
@Tag(name = "Reviews", description = "Query stored reviews")
public class ReviewsController {

    private final ReviewFetchService reviewFetchService;

    public ReviewsController(ReviewFetchService reviewFetchService) {
        this.reviewFetchService = reviewFetchService;
    }

    @GetMapping("/reviews")
    @Operation(summary = "List stored reviews")
    public ResponseEntity<List<ReviewResponse>> list(
            @Parameter(description = "Google Play package id", required = true)
            @RequestParam String packageId,
            @Parameter(description = "Filter by country (gl code)")
            @RequestParam(required = false) String country,
            @Parameter(description = "Max rows", example = "50")
            @RequestParam(defaultValue = "50") int limit) {
        List<ReviewResponse> reviews = reviewFetchService.listReviews(packageId, country, limit);
        return ResponseEntity.ok(reviews);
    }
}
