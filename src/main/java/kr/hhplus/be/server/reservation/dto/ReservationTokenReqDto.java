package kr.hhplus.be.server.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationTokenReqDto {
    private UUID userId;
    private int concertId;
}
