package ru.yandex.practicum.accounts.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient.Builder notificationsWebClient(@Value("${client.notifications.url}") String url) {
        return WebClient.builder()
                .baseUrl(url);
    }
}
