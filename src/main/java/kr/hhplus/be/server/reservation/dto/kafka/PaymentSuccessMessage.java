package kr.hhplus.be.server.reservation.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessMessage {
    private int concertScheduleId;
    private UUID userId;
    private UUID tokenId;
    private int seatId;
}
