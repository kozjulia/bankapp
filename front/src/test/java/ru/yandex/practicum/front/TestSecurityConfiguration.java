package ru.yandex.practicum.front;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfiguration {

    @Bean
    @Primary
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId("front-client")
                .clientId("front-client")
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
