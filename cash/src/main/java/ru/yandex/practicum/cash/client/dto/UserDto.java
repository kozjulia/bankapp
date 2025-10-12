package ru.yandex.practicum.cash.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String login;
    private String name;
    private String password;
    private String birthdate;
    private List<AccountDto> accounts;
}
