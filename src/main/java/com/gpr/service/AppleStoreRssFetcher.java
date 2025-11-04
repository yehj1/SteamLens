package com.gpr.service;

import com.gpr.domain.Review;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * TODO: implement Apple App Store review ingestion via RSS feed.
 * This class is a placeholder to highlight the extension point for additional data sources.
 */
public class AppleStoreRssFetcher implements ReviewFetcher {
    @Override
    public List<Review> fetch(String packageId, String country, String lang, int limit) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public String sourceName() {
        return "apple_app_store";
    }
}
