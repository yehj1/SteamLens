package com.gpr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gpr")
public class GprProperties {

    private String apiKey;
    private final SerpApiProperties serpapi = new SerpApiProperties();
    private final VolcProperties volc = new VolcProperties();
    private final SteamProperties steam = new SteamProperties();

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public SerpApiProperties getSerpapi() {
        return serpapi;
    }

    public VolcProperties getVolc() {
        return volc;
    }

    public SteamProperties getSteam() {
        return steam;
    }

    public static class SerpApiProperties {
        private String baseUrl = "https://serpapi.com";
        private String apiKey;
        private String engine = "google_play_product";
        private String defaultLang = "en";
        private String defaultCountries = "us";
        private int maxRetries = 1;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getEngine() {
            return engine;
        }

        public void setEngine(String engine) {
            this.engine = engine;
        }

        public String getDefaultLang() {
            return defaultLang;
        }

        public void setDefaultLang(String defaultLang) {
            this.defaultLang = defaultLang;
        }

        public String getDefaultCountries() {
            return defaultCountries;
        }

        public void setDefaultCountries(String defaultCountries) {
            this.defaultCountries = defaultCountries;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
    }

    public static class VolcProperties {
        private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";
        private String apiKey;
        private String model = "doubao-pro-32k";
        private long timeoutMs = 30000;
        private double temperature = 0.1;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }

    public static class SteamProperties {
        private String baseUrl = "https://store.steampowered.com";
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";
        private String cookie = "Steam_Language=english;timezoneOffset=28800,0";
        private int connectTimeoutSeconds = 10;
        private int requestTimeoutSeconds = 15;
        private String proxyHost;
        private Integer proxyPort;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public String getCookie() {
            return cookie;
        }

        public void setCookie(String cookie) {
            this.cookie = cookie;
        }

        public int getConnectTimeoutSeconds() {
            return connectTimeoutSeconds;
        }

        public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
            this.connectTimeoutSeconds = connectTimeoutSeconds;
        }

        public int getRequestTimeoutSeconds() {
            return requestTimeoutSeconds;
        }

        public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
            this.requestTimeoutSeconds = requestTimeoutSeconds;
        }

        public String getProxyHost() {
            return proxyHost;
        }

        public void setProxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
        }

        public Integer getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(Integer proxyPort) {
            this.proxyPort = proxyPort;
        }
    }
}
