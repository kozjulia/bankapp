package ru.yandex.practicum.front.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EditPasswordRequest {

    private String login;
    private String password;
    private String confirmPassword;
}
