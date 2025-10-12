package ru.yandex.practicum.cash.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    @LoadBalanced
    public WebClient.Builder accountsWebClient(@Value("${client.accounts.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl);
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder blockerWebClient(@Value("${client.blocker.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl);
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder notificationsWebClient(@Value("${client.notifications.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl);
    }
}
