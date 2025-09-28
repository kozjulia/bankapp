package ru.yandex.practicum.front.client.dto;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.front.dto.AccountDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserDto {

    private UUID id;
    private String login;
    private String name;
    private String password;
    private LocalDate birthdate;
    private List<AccountDto> accounts;
}
