package ru.yandex.practicum.front.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    private String login;
    private String name;
    private String password;
    private String confirmPassword;
    private String birthdate;
}
