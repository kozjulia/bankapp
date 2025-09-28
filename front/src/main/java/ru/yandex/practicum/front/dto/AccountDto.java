package ru.yandex.practicum.front.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AccountDto {

    private UUID id;
    private CurrencyDto currency;
    private Integer value;
    private boolean exists;
}
