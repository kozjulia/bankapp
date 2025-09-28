package ru.yandex.practicum.transfer.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@EnableWebFluxSecurity
@TestConfiguration
public class TestSecurityConfiguration {

    @Bean
    @Primary
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return token -> Mono.just(new Jwt(
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("1", "1"),
                Map.of(
                        "sub", "test-user",
                        "scope", "openid profile",
                        "roles", List.of("USER")
                )
        ));
    }

    @Bean
    @Primary
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId("keycloak")
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("http://fake-auth")
                .tokenUri("http://fake-token")
                .userInfoUri("http://fake-userinfo")
                .userNameAttributeName("sub")
                .clientName("Test Client")
                .build();

        return new InMemoryReactiveClientRegistrationRepository(registration);
    }
}
