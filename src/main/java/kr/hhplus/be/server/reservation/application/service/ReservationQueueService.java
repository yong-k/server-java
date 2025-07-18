package kr.hhplus.be.server.reservation.application.service;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.reservation.application.port.in.ReservationQueueUseCase;
import kr.hhplus.be.server.reservation.application.port.out.ReservationQueueStore;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.config.ReservationQueueProperties;
import kr.hhplus.be.server.reservation.config.ReservationTokenProperties;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import kr.hhplus.be.server.reservation.dto.QueueEnterRespDto;
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
    private final ReservationTokenProperties reservationTokenProperties;
    private final ReservationQueueProperties reservationQueueProperties;

    @Override
    @Transactional
    public QueueEnterRespDto enterQueue(UUID tokenId) {
        // 1. 토큰 조회
        ReservationToken token = reservationTokenRepository.findById(tokenId)
                .orElseThrow(() -> new DataNotFoundException("대기열토큰이 존재하지 않습니다: tokenId = " + tokenId));

        // 2. 대기열큐에 추가 (이미 대기열 내에 allowed 상태의 tokenId가 존재하면 추가하지 않음)
        ReservationTokenStatus status = token.getStatus();
        int position = -1;
        if (status != ReservationTokenStatus.ALLOWED) {
            // 대기열큐에 추가
            position = reservationQueueStore.addToQueueIfAbsent(tokenId, System.currentTimeMillis());

            // 순번이 대기큐의 허용범위 내이고, 현재 WAITING 상태면 ==> ALLOWED로 전환
            if (position <= reservationQueueProperties.getAllowedLimit() && status == ReservationTokenStatus.WAITING) {
                token.allow(reservationTokenProperties.getAllowedToTimeoutMinutes());   // Dirty Checking OK
                status = token.getStatus();
            }
        } else {
            // 이미 allowed 상태의 tokenId가 있다면, 현재 순번만 조회
            position = reservationQueueStore.getQueuePosition(tokenId);
        }

        // 3. ALLOWED 상태일 경우 TTL 계산
        Integer ttlSeconds = null;
        if (status == ReservationTokenStatus.ALLOWED && token.getExpiredAt() != null) {
            ttlSeconds = (int) Duration.between(LocalDateTime.now(), token.getExpiredAt()).getSeconds();
            if (ttlSeconds < 0) ttlSeconds = 0;     // 음수 방지
        }

        return QueueEnterRespDto.builder()
                .status(status)
                .position(position)
                .ttlSeconds(ttlSeconds)
                .build();
    }
}
