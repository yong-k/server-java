package kr.hhplus.be.server.reservation.infrastructure.kafka;

import kr.hhplus.be.server.reservation.dto.kafka.PaymentSuccessMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSuccessKafkaProducer {
    private final KafkaTemplate<String, PaymentSuccessMessage> kafkaTemplate;

    private static final String TOPIC = "payment_success";

    public void publish(PaymentSuccessMessage message) {
        kafkaTemplate.send(TOPIC, message);
    }
}
