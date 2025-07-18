package kr.hhplus.be.server.reservation.domain;

import kr.hhplus.be.server.concert.domain.Seat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentSuccessEvent {
    private final int concertScheduleId;
    private final UUID userId;
    private final UUID tokenId;
    private final Seat seat;
}
