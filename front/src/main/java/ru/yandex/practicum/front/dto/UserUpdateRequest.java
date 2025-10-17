package ru.yandex.practicum.front.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserUpdateRequest {

    private String name;
    private String birthdate;
    private List<String> account = new ArrayList<>();
}
