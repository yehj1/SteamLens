package com.gpr.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(GprProperties.class)
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean(name = "serpApiWebClient")
    public WebClient serpApiWebClient(GprProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(properties.getSerpapi().getMaxRetries() > 0 ? 10000 : 5000));
        return WebClient.builder()
                .baseUrl(properties.getSerpapi().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    @Bean(name = "volcWebClient")
    public WebClient volcWebClient(GprProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(properties.getVolc().getTimeoutMs()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(
                                Math.max(1, Math.toIntExact(properties.getVolc().getTimeoutMs() / 1000))))
                        .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(
                                Math.max(1, Math.toIntExact(properties.getVolc().getTimeoutMs() / 1000)))));
        return WebClient.builder()
                .baseUrl(properties.getVolc().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
