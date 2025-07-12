package kr.hhplus.be.server.reservation.dto;

import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationTokenRespDto {
    private UUID id;
    private UUID userId;
    private int concertId;
    private ReservationTokenStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiredAt;

    public static ReservationTokenRespDto from(ReservationToken token) {
        return ReservationTokenRespDto.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .status(token.getStatus())
                .issuedAt(token.getIssuedAt())
                .updatedAt(token.getUpdatedAt())
                .expiredAt(token.getExpiredAt())
                .build();
    }
}
