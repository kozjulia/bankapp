package ru.yandex.practicum.front.client.dto;

import lombok.Data;

@Data
public class TransferRequest {

    private String login;
    private String fromCurrency;
    private String toCurrency;
    private Integer value;
    private String toLogin;
}
