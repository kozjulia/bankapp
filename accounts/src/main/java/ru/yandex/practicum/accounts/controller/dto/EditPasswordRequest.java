package ru.yandex.practicum.accounts.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditPasswordRequest {

    private String password;
    private String confirmPassword;
}
