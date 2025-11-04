package com.gpr.controller;

import com.gpr.dto.FetchRequest;
import com.gpr.dto.FetchResponse;
import com.gpr.dto.SteamFetchRequest;
import com.gpr.dto.SteamFetchResponse;
import com.gpr.service.ReviewFetchService;
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
@RequestMapping("/fetch")
@Tag(name = "Fetch", description = "Ingest reviews from external sources")
public class FetchController {

    private final ReviewFetchService reviewFetchService;

    public FetchController(ReviewFetchService reviewFetchService) {
        this.reviewFetchService = reviewFetchService;
    }

    @PostMapping("/google-play")
    @Operation(summary = "Fetch Google Play reviews via SerpApi and store them")
    public ResponseEntity<FetchResponse> fetchGooglePlay(@Valid @RequestBody FetchRequest request) throws IOException {
        FetchResponse response = reviewFetchService.fetchGooglePlay(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/steam")
    @Operation(summary = "Fetch Steam reviews via Steam store API and store them")
    public ResponseEntity<SteamFetchResponse> fetchSteam(@Valid @RequestBody SteamFetchRequest request) throws Exception {
        SteamFetchResponse response = reviewFetchService.fetchSteam(request);
        return ResponseEntity.ok(response);
    }
}
