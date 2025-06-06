package kr.hhplus.be.server.reservation.dto;

import kr.hhplus.be.server.concert.domain.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRespDto {
    private UUID userId;
    private int amount;

    private int seatId;
    private UUID seatUserId;
    private SeatStatus seatStatus;
}
