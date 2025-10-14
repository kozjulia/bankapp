package ru.yandex.practicum.transfer.controller.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    private String login;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal value;
    private String toLogin;
}
