package kr.hhplus.be.server.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationTokenReqDto {

    @NotNull(message = "사용자ID는 필수입니다")
    private UUID userId;

    @NotNull(message = "콘서트ID는 필수입니다")
    private Integer concertId;
}
