package kr.hhplus.be.server.reservation.application.service;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.reservation.application.port.in.ReservationQueueUseCase;
import kr.hhplus.be.server.reservation.application.port.out.ReservationQueueStore;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import kr.hhplus.be.server.reservation.dto.QueueEnterRespDto;
import kr.hhplus.be.server.reservation.exception.InvalidReservationTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationQueueService implements ReservationQueueUseCase {

    private final ReservationQueueStore reservationQueueStore;
    private final ReservationTokenRepository reservationTokenRepository;

    @Override
    @Transactional
    public QueueEnterRespDto enterQueue(UUID tokenId) {
        // 1. 토큰 조회
        ReservationToken token = reservationTokenRepository.findById(tokenId)
                .orElseThrow(() -> new DataNotFoundException("대기열토큰이 존재하지 않습니다: tokenId = " + tokenId));

        // 2. 대기열큐에 추가
        ReservationTokenStatus status = token.getStatus();
        int position = -1;
        Integer ttlSeconds = null;

        if (status == ReservationTokenStatus.WAITING) {
            position = reservationQueueStore.addToQueueIfAbsent(tokenId, System.currentTimeMillis());
        } else if (status == ReservationTokenStatus.ALLOWED && token.getExpiredAt() != null) {
            ttlSeconds = (int) Duration.between(LocalDateTime.now(), token.getExpiredAt()).getSeconds();
            if (ttlSeconds < 0) ttlSeconds = 0;     // 음수 방지
        } else {    // TIMEOUT, COMPLETED
            throw new InvalidReservationTokenException("대기열 진입이 불가능한 토큰 상태입니다: tokenId=" + tokenId + ", status=" + status);
        }

        return QueueEnterRespDto.builder()
                .status(status)
                .position(position)
                .ttlSeconds(ttlSeconds)
                .build();
    }
}
