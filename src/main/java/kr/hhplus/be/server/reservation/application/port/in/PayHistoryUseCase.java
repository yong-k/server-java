package kr.hhplus.be.server.reservation.application.port.in;

import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.reservation.domain.PaymentReason;
import kr.hhplus.be.server.reservation.domain.PaymentStatus;

import java.util.UUID;

public interface PayHistoryUseCase {
    void saveSuccessHistory(Seat seat, UUID userId, PaymentStatus status, PaymentReason reason);

    void saveFailedHistory(Seat seat, UUID userId, PaymentStatus status, PaymentReason reason);
}
