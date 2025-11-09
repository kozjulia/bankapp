package ru.yandex.practicum.cash.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.cash.mq.dto.NotificationRequest;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    public void sendNotification(NotificationRequest request) {
        kafkaTemplate.send("notifications", request);
    }
}
