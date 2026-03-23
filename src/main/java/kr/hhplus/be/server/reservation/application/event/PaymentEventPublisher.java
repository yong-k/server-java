package kr.hhplus.be.server.reservation.application.event;

import kr.hhplus.be.server.reservation.domain.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Kafka 도입 전 사용 코드 (현재 사용 X)
 */
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void success(PaymentSuccessEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
