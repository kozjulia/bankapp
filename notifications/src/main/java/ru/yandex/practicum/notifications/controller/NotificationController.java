package ru.yandex.practicum.notifications.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.notifications.controller.dto.NotificationRequest;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @PostMapping
    public Mono<String> sendNotification(@RequestBody NotificationRequest request) {
        log.info("Уведомление пользователю {}: {}", request.login(), request.message());
        return Mono.just("Уведомление отправлено");
    }
}
