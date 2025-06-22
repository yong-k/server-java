package kr.hhplus.be.server.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeatReservationReqDto {

    @NotNull(message = "좌석ID는 필수입니다")
    private Integer seatId;

    @NotNull(message = "사용자ID는 필수입니다")
    private UUID userId;
}
