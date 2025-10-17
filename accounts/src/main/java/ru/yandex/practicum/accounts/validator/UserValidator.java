package ru.yandex.practicum.accounts.validator;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.controller.dto.UserDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

@UtilityClass
public class UserValidator {

    private static final String INVALID_BIRTHDAY_ERROR = "Пользователь должен быть старше 18 лет";
    public static final String INVALID_BIRTHDAY_FORMAT_ERROR = "Неверный формат даты";

    public static Mono<ResponseStatusException> validatePasswordChange(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароль и подтверждение пароля не совпадают"));
        } else {
            return Mono.empty();
        }
    }

    public static Mono<ResponseStatusException> validateBirthdate(String birthdate) {
        if (birthdate == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата рождения должна быть заполнена"));
        }

        try {
            LocalDate date = LocalDate.parse(birthdate);
            OffsetDateTime birthdateOffset = date.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime eighteenYearsAgo = OffsetDateTime.now(ZoneOffset.UTC).minusYears(18);

            if (birthdateOffset.isAfter(eighteenYearsAgo)) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_BIRTHDAY_ERROR));
            }
            return Mono.empty();
        } catch (DateTimeParseException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_BIRTHDAY_FORMAT_ERROR));
        }
    }

    public static Mono<ResponseStatusException> validateBirthdate(LocalDate birthdate) {
        if (birthdate == null) {
            return Mono.empty();
        }

        try {
            LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);

            if (birthdate.isAfter(eighteenYearsAgo)) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_BIRTHDAY_ERROR));
            }
            return Mono.empty();
        } catch (DateTimeParseException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_BIRTHDAY_FORMAT_ERROR));
        }
    }

    public static Mono<Void> validateAccounts(UserDto user) {
        boolean hasInvalidAccounts = user.getAccounts().stream()
                .anyMatch(account -> !account.isExists() && account.getValue().compareTo(BigDecimal.ZERO) != 0);

        if (hasInvalidAccounts) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Для отключенных аккаунтов (exists=false) значение value должно быть 0.00"));
        }
        return Mono.empty();
    }
}