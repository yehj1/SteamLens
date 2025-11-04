package com.gpr.service;

import com.gpr.config.GprProperties;
import com.gpr.domain.App;
import com.gpr.domain.Review;
import com.gpr.domain.ReviewSource;
import com.gpr.dto.FetchRequest;
import com.gpr.dto.FetchResponse;
import com.gpr.dto.ReviewResponse;
import com.gpr.dto.SteamFetchRequest;
import com.gpr.dto.SteamFetchResponse;
import com.gpr.repo.AppRepository;
import com.gpr.repo.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewFetchService {

    private static final Logger log = LoggerFactory.getLogger(ReviewFetchService.class);

    private final Map<String, ReviewFetcher> fetchers;
    private final AppRepository appRepository;
    private final ReviewRepository reviewRepository;
    private final GprProperties properties;

    public ReviewFetchService(Map<String, ReviewFetcher> fetchers,
                              AppRepository appRepository,
                              ReviewRepository reviewRepository,
                              GprProperties properties) {
        this.fetchers = fetchers;
        this.appRepository = appRepository;
        this.reviewRepository = reviewRepository;
        this.properties = properties;
    }

    @Transactional
    public FetchResponse fetchGooglePlay(FetchRequest request) throws IOException {
        ReviewFetcher fetcher = resolveFetcher("google_play");
        String packageId = request.getPackageId();
        String lang = Optional.ofNullable(request.getLang())
                .filter(s -> !s.isBlank())
                .orElse(properties.getSerpapi().getDefaultLang());
        List<String> countries = resolveCountries(request.getCountries());
        int limitPerCountry = Optional.ofNullable(request.getLimitPerCountry()).orElse(100);

        App app = appRepository.upsert(packageId, null);

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String country : countries) {
            List<Review> fetched = fetcher.fetch(packageId, country, lang, limitPerCountry);
            int inserted = 0;
            for (Review review : fetched) {
                review.setAppId(app.getId());
                if (review.getLang() == null || review.getLang().isBlank()) {
                    review.setLang(lang);
                }
                inserted += reviewRepository.saveIfNotExists(review);
            }
            counts.put(country, inserted);
            log.info("Inserted {} reviews for {} ({})", inserted, packageId, country);
        }
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        return new FetchResponse(packageId, counts, total);
    }

    public List<ReviewResponse> listReviews(String packageId, String country, int limit) {
        App app = appRepository.findByPackageId(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown packageId: " + packageId));
        if (limit <= 0) {
            limit = 50;
        }
        List<com.gpr.domain.Review> reviews = reviewRepository.findByAppAndCountry(app.getId(), country, limit);
        List<ReviewResponse> response = new ArrayList<>(reviews.size());
        for (Review review : reviews) {
            response.add(new ReviewResponse(
                    review.getId(),
                    packageId,
                    review.getReviewSource(),
                    review.getCountry(),
                    review.getLang(),
                    review.getRating(),
                    review.getAuthor(),
                    review.getContent(),
                    review.getReviewedAt()
            ));
        }
        return response;
    }

    @Transactional
    public SteamFetchResponse fetchSteam(SteamFetchRequest request) throws Exception {
        ReviewFetcher fetcher = resolveFetcher(ReviewSource.STEAM.value());

        UUID appUuid;
        if (request.getAppUuid() != null && !request.getAppUuid().isBlank()) {
            appUuid = UUID.fromString(request.getAppUuid());
            appRepository.ensureExists(appUuid, request.getAppId());
        } else {
            App app = appRepository.upsert(request.getAppId(), null);
            appUuid = app.getId();
        }

        ReviewFetcher.FetchRequest fetchRequest = new ReviewFetcher.FetchRequest(
                request.getAppId(),
                request.getLang(),
                request.getLimit(),
                request.getFilter(),
                request.getReviewType(),
                request.getPurchaseType(),
                request.getDayRange()
        );

        List<ReviewFetcher.UnifiedReview> items = fetcher.fetch(fetchRequest);
        int saved = 0;
        for (ReviewFetcher.UnifiedReview item : items) {
            if (item.externalId() == null || item.externalId().isBlank()) {
                continue;
            }
            if (reviewRepository.existsBySourceAndRecommendationId(fetcher.sourceName(), item.externalId())) {
                continue;
            }
            Review review = toReview(item, appUuid);
            reviewRepository.saveIfNotExists(review);
            saved++;
        }
        return new SteamFetchResponse(appUuid, fetcher.sourceName(), saved);
    }

    private ReviewFetcher resolveFetcher(String name) {
        return fetchers.values().stream()
                .filter(fetcher -> fetcher.sourceName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Fetcher not found: " + name));
    }

    private List<String> resolveCountries(List<String> requested) {
        if (!CollectionUtils.isEmpty(requested)) {
            return requested;
        }
        String defaults = properties.getSerpapi().getDefaultCountries();
        if (defaults == null || defaults.isBlank()) {
            return List.of("us");
        }
        String[] tokens = defaults.split(",");
        List<String> countries = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isBlank()) {
                countries.add(token.trim().toLowerCase(Locale.ROOT));
            }
        }
        return countries.isEmpty() ? List.of("us") : countries;
    }

    private Review toReview(ReviewFetcher.UnifiedReview item, UUID appUuid) {
        Review review = new Review();
        review.setId(reviewId(item.source(), item.externalId()));
        review.setAppId(appUuid);
        review.setReviewSource(item.source());
        review.setExternalId(item.externalId());
        review.setRecommendationId(item.externalId());
        review.setLang(item.lang());
        review.setRating(item.rating());
        review.setContent(item.content());
        review.setReviewedAt(item.createdAt());
        review.setExtraJson(item.rawJson());
        review.setAuthorId(item.authorId());
        review.setPlaytimeForever(item.playtimeForever());
        return review;
    }

    private UUID reviewId(String source, String externalId) {
        if (externalId == null || externalId.isBlank()) {
            return UUID.randomUUID();
        }
        String key = source + "|" + externalId;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }
}
