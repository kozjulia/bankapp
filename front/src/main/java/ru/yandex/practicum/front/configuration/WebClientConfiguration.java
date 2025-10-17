package ru.yandex.practicum.front.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    @LoadBalanced
    public WebClient.Builder accountsWebClient(@Value("${client.accounts.url}") String url) {
        return WebClient.builder()
                .baseUrl(url);
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder cashWebClient(@Value("${client.cash.url}") String url) {
        return WebClient.builder()
                .baseUrl(url);
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder exchangeWebClient(@Value("${client.exchange.url}") String url) {
        return WebClient.builder()
                .baseUrl(url);
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder transferWebClient(@Value("${client.transfer.url}") String url) {
        return WebClient.builder()
                .baseUrl(url);
    }
}
