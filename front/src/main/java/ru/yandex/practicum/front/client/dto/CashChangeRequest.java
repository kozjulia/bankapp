package ru.yandex.practicum.front.client.dto;

import lombok.Data;

@Data
public class CashChangeRequest {

    private String currency;
    private Integer value;
    private Action action;

    public enum Action {
        PUT, GET
    }
}
