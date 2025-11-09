package ru.yandex.practicum.exchange.generator.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exchange.generator.mq.dto.CurrencyDto;

@Service
@RequiredArgsConstructor
public class CurrencyRateProducer {

    private final KafkaTemplate<String, CurrencyDto> kafkaTemplate;

    public void sendCurrencyRate(CurrencyDto currency) {
        kafkaTemplate.send("currency-rates", currency);
    }
}
