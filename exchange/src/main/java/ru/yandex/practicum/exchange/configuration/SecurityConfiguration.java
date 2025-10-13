package ru.yandex.practicum.exchange.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity security) {
        return security
                .authorizeExchange(requests -> requests
                        .pathMatchers("/actuator/*").permitAll()
                        .pathMatchers("/api/currencies/**").hasAnyAuthority("SCOPE_exchange.write", "SCOPE_exchange.read")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(serverSpec -> serverSpec
                        .jwt(jwtSpec -> {
                            ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
                            converter.setJwtGrantedAuthoritiesConverter(
                                    new ReactiveJwtGrantedAuthoritiesConverterAdapter(
                                            new JwtGrantedAuthoritiesConverter()
                                    )
                            );
                            jwtSpec.jwtAuthenticationConverter(converter);
                        })
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}
