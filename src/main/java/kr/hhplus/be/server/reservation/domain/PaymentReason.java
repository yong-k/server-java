package kr.hhplus.be.server.reservation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentReason {
    INVALID_SEAT_STATUS("임시배정상태 아님"),
    INVALID_USER("해당 사용자에게 배정된 좌석이 아님"),
    USER_CANCELED("사용자 취소");

    private final String message;
}
