package kr.hhplus.be.server.reservation.application.port.in;

import kr.hhplus.be.server.reservation.dto.QueueEnterRespDto;

import java.util.UUID;

public interface ReservationQueueUseCase {

    /**
     * 대기열에 사용자 진입
     * @param tokenId 대기열토큰 ID
     * @return 진입 결과 (상태, 순번 등)
     */
    QueueEnterRespDto enterQueue(UUID tokenId);
}
