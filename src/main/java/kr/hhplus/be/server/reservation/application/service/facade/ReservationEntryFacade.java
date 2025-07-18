package kr.hhplus.be.server.reservation.application.service.facade;

import kr.hhplus.be.server.reservation.application.service.ReservationQueueService;
import kr.hhplus.be.server.reservation.application.service.ReservationService;
import kr.hhplus.be.server.reservation.dto.QueueEnterRespDto;
import kr.hhplus.be.server.reservation.dto.ReservationTokenRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationEntryFacade {

    private final ReservationService reservationService;
    private final ReservationQueueService queueService;

    /**
     * 예약서비스 진입 요청 시, 대기열토큰 발급 + 대기열 진입
     *
     * @param userId 사용자 ID
     * @return 대기열 상태 + 순번 + TTL 정보
     */
    public QueueEnterRespDto issueTokenAndEnterQueue(UUID userId) {
        ReservationTokenRespDto token = reservationService.issueToken(userId);
        return queueService.enterQueue(token.getId());
    }
}
