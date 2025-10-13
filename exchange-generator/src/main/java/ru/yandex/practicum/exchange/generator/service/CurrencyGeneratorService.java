package ru.yandex.practicum.exchange.generator.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.exchange.generator.client.ExchangeClient;
import ru.yandex.practicum.exchange.generator.client.dto.CurrencyDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Service
@EnableScheduling
public class CurrencyGeneratorService {

    private final Random random;
    private final List<String> currencyCodes;
    private final ExchangeClient exchangeClient;

    public CurrencyGeneratorService(ExchangeClient exchangeClient) {
        this.exchangeClient = exchangeClient;
        this.random = new Random();
        this.currencyCodes = List.of("USD", "CNY", "RUB");
    }

    @Scheduled(cron = "* */5 * * * *")
    public void generateAndUpdateCurrencyRates() {
        currencyCodes.forEach(code -> {
            BigDecimal randomValue = BigDecimal.valueOf(1.0)
                    .add(BigDecimal.valueOf(random.nextDouble()).multiply(BigDecimal.valueOf(2.0)));
            BigDecimal roundedRate = randomValue.setScale(2, RoundingMode.HALF_UP);
            CurrencyDto updatedCurrency = new CurrencyDto(code, getTitleForCode(code), roundedRate);
            exchangeClient.updateCurrencyRate(code, Mono.just(updatedCurrency))
                    .subscribe();
        });
    }

    private String getTitleForCode(String code) {
        return switch (code) {
            case "USD" -> "Dollars";
            case "CNY" -> "Yuan";
            case "RUB" -> "Rubles";
            default -> code;
        };
    }
}

