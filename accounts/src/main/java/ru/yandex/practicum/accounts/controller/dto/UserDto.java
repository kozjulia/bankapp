package ru.yandex.practicum.accounts.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    private String login;

    private String name;

    private String password;

    private LocalDate birthdate;

    @Builder.Default
    private List<AccountDto> accounts = new ArrayList<>();
}
