package ru.yandex.practicum.accounts.service.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.accounts.BaseIntegrationTest;
import ru.yandex.practicum.accounts.TestSecurityConfiguration;
import ru.yandex.practicum.accounts.controller.dto.AccountDto;
import ru.yandex.practicum.accounts.controller.dto.CurrencyDto;
import ru.yandex.practicum.accounts.controller.dto.CurrencyEnum;
import ru.yandex.practicum.accounts.controller.dto.EditPasswordRequest;
import ru.yandex.practicum.accounts.controller.dto.UserCreateRequest;
import ru.yandex.practicum.accounts.controller.dto.UserDto;
import ru.yandex.practicum.accounts.mapper.UserMapper;
import ru.yandex.practicum.accounts.mq.dto.NotificationRequest;
import ru.yandex.practicum.accounts.repository.UserRepository;
import ru.yandex.practicum.accounts.service.AccountService;
import ru.yandex.practicum.accounts.service.UserService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

@Import(TestSecurityConfiguration.class)
class UserServiceImplTest extends BaseIntegrationTest {

    @MockitoBean
    private ReactiveOAuth2AuthorizedClientManager manager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @BeforeEach
    void setUp() {
        OAuth2AccessToken token = new OAuth2AccessToken(
                BEARER, "mock-token", Instant.now(), Instant.now().plusSeconds(300));

        when(manager.authorize(any()))
                .thenReturn(Mono.just(new OAuth2AuthorizedClient(
                        mock(ClientRegistration.class),
                        "principal",
                        token)));
    }

    @Test
    void createUserWhenAllParamsAreValidThenCreateUserTest() {
        LocalDate birthDate = LocalDate.of(1998, 10, 10);
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .login("test_user")
                .password("test_password")
                .confirmPassword("test_password")
                .name("test_name")
                .birthdate(birthDate.toString())
                .build();

        List<AccountDto> expectedAccounts = Arrays.stream(CurrencyEnum.values())
                .map(currency -> AccountDto.builder()
                        .currency(new CurrencyDto(currency.name()))
                        .value(BigDecimal.ZERO)
                        .exists(true)
                        .build())
                .toList();

        UserDto expectedUser = UserDto.builder()
                .login("test_user")
                .name("test_name")
                .birthdate(birthDate)
                .accounts(expectedAccounts)
                .build();

        StepVerifier.create(userService.createUser(userCreateRequest))
                .assertNext(actualUser -> {
                    Assertions.assertThat(actualUser)
                            .usingRecursiveComparison()
                            .ignoringFields("id", "accounts", "password")
                            .isEqualTo(expectedUser);

                    Assertions.assertThat(actualUser.getAccounts())
                            .usingRecursiveComparison()
                            .ignoringFields("id")
                            .ignoringCollectionOrder()
                            .isEqualTo(expectedUser.getAccounts());
                })
                .verifyComplete();
    }

    @Test
    void updateUserPasswordWhenPasswordIsValidThenUpdatePasswordTest() {
        String login = "test_user";
        String oldPassword = "old_password";
        String newPassword = "new_password";

        UserCreateRequest createRequest = UserCreateRequest.builder()
                .login(login)
                .password(oldPassword)
                .confirmPassword(oldPassword)
                .name("Test User")
                .birthdate("1990-01-01")
                .build();

        EditPasswordRequest editRequest = EditPasswordRequest.builder()
                .password(newPassword)
                .confirmPassword(newPassword)
                .build();

        KafkaConsumer<String, NotificationRequest> consumer = createConsumer();

        ConsumerRecords<String, NotificationRequest> dummyRecords = consumer.poll(Duration.ofMillis(100));
        consumer.seekToEnd(consumer.assignment());

        StepVerifier.create(userService.createUser(createRequest)
                        .then(userService.updateUserPassword(login, editRequest)))
                .assertNext(updatedUser -> {
                    Assertions.assertThat(updatedUser.getLogin()).isEqualTo(login);
                    Assertions.assertThat(updatedUser.getPassword()).isEqualTo(newPassword);
                })
                .verifyComplete();

        StepVerifier.create(userRepository.findByLogin(login))
                .assertNext(userEntity ->
                        Assertions.assertThat(userEntity.getPassword()).isEqualTo(newPassword)
                )
                .verifyComplete();

        ConsumerRecord<String, NotificationRequest> record =
                KafkaTestUtils.getSingleRecord(consumer, "notifications", Duration.ofSeconds(5));

        Assertions.assertThat(record).isNotNull();
        Assertions.assertThat(record.value().login()).isEqualTo(login);
        Assertions.assertThat(record.value().message()).isEqualTo("Пароль успешно обновлен");
    }

    private KafkaConsumer<String, NotificationRequest> createConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup" + UUID.randomUUID(), "true", embeddedKafka);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        KafkaConsumer<String, NotificationRequest> consumer = new KafkaConsumer<>(
                consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(NotificationRequest.class)
        );
        consumer.subscribe(Collections.singleton("notifications"));
        return consumer;
    }
}