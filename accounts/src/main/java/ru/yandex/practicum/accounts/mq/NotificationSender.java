package ru.yandex.practicum.accounts.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import ru.yandex.practicum.accounts.mq.dto.NotificationRequest;

@Service
@RequiredArgsConstructor
public class NotificationSender {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    public void sendNotification(NotificationRequest request) {
        kafkaTemplate.send("notifications", request);
    }
}
