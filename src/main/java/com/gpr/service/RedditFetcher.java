package com.gpr.service;

import com.gpr.domain.Review;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * TODO: implement Reddit discussion ingestion (e.g. subreddit threads).
 * Provides a stub that can be wired into ReviewFetchService once ready.
 */
public class RedditFetcher implements ReviewFetcher {
    @Override
    public List<Review> fetch(String packageId, String country, String lang, int limit) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public String sourceName() {
        return "reddit";
    }
}
