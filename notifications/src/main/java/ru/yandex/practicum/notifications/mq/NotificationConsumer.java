package ru.yandex.practicum.notifications.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.notifications.mq.dto.NotificationRequest;

@Slf4j
@Service
public class NotificationConsumer {

    @KafkaListener(topics = "notifications", groupId = "notifications")
    public void listen(@Payload NotificationRequest request) {
        log.info("Уведомление пользователю {}: {}", request.login(), request.message());
    }
}
