package ru.yandex.practicum.front.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.client.AccountsClient;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final AccountsClient accountsClient;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return accountsClient.getAccountDetails(username)
                .map(account ->
                        CustomUserDetails.customUserDetailsBuilder()
                                .username(account.getLogin())
                                .password(passwordEncoder.encode(account.getPassword()))
                                .authorities(Arrays.stream("USER".split(","))
                                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList())
                                .accountNonExpired(true)
                                .credentialsNonExpired(true)
                                .accountNonLocked(true)
                                .enabled(true)
                                .build()
                );
    }
}
