package kr.hhplus.be.server.reservation.dto;

import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEnterRespDto {

    /**
     * 사용자 상태: WAITING, ALLOWED, TIMEOUT, COMPLETED
     * - 대기열토큰 상태와 동일
     */
    private ReservationTokenStatus status;

    /**
     * 대기열 내 순번
     */
    private int position;

    /**
     * ALLOWED 상태일 경우, TIMEOUT 까지 남은 TTL (초)
     */
    private Integer ttlSeconds;
}
