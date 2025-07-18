package kr.hhplus.be.server.reservation.scheduler;

import kr.hhplus.be.server.reservation.application.port.out.ReservationQueueStore;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.config.ReservationQueueProperties;
import kr.hhplus.be.server.reservation.config.ReservationTokenProperties;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationTokenScheduler {

    private final ReservationTokenRepository reservationTokenRepository;
    private final ReservationQueueStore reservationQueueStore;
    private final RetryTemplate retryTemplate;

    private final ReservationTokenProperties reservationTokenProperties;
    private final ReservationQueueProperties reservationQueueProperties;


    /**
     * ALLOWED 상태에서 TTL 지난 토큰은 TIMEOUT 처리
     */
    @Transactional
    @Scheduled(fixedDelayString = "${reservation.token.scheduler-interval-ms:60000}")

    public void timeoutAllowedTokens() {
        retryTemplate.execute(context -> {
            LocalDateTime now = LocalDateTime.now();

            // ALLOWED 상태에서 10분간 결제되지 않음 → TIMEOUT
            List<ReservationToken> toTimeout = reservationTokenRepository.findByStatusAndExpiredAtBefore(ReservationTokenStatus.ALLOWED, now);
            toTimeout.forEach(ReservationToken::timeout);
            return null;
        }, context -> {
            log.error("대기열토큰 TIMEOUT 처리 실패 - ReservationTokenScheduler 최대 재시도 횟수 초과");
            return null;
        });
    }

    /**
     * WAITING 상태 중 순번 상위권 토큰을 ALLOWED로 변경
     */
    @Transactional
    @Scheduled(fixedDelayString = "${reservation.queue.scheduler-interval-ms:50000}")
    public void allowWaitingTokens() {
        retryTemplate.execute(context -> {
            // 대기열에 존재하는 WAITING 토큰 목록 조회 (한 번에 너무 많이 가져올 수 있으니, 제한 걸음)
            int batchAllowedLimit = reservationQueueProperties.getBatchAllowedLimit();
            List<UUID> queue = reservationQueueStore.getWaitingQueueTokenIds(batchAllowedLimit);
            if (queue.isEmpty()) return null;

            // 예약서비스 입장 제한 인원
            int allowedLimit = reservationQueueProperties.getAllowedLimit();
            // 현재 ALLOWED 인원 수
            long allowedCount = reservationTokenRepository.countByStatus(ReservationTokenStatus.ALLOWED);

            for (UUID tokenId : queue) {
                if (allowedCount >= allowedLimit) break;

                ReservationToken token = reservationTokenRepository.findById(tokenId).orElse(null);
                if (token == null || token.getStatus() != ReservationTokenStatus.WAITING) continue;

                token.allow(reservationTokenProperties.getAllowedToTimeoutMinutes());   // Dirty Checking OK
                allowedCount++;
            }
            return null;
        }, context -> {
            log.error("대기열토큰 ALLOWED 처리 실패 - ReservationTokenScheduler 최대 재시도 횟수 초과");
            return null;
        });
    }
}
