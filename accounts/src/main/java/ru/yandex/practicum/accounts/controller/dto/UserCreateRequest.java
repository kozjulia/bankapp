package ru.yandex.practicum.accounts.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    private String login;
    private String name;
    private String password;
    private String confirmPassword;
    private String birthdate;
}
