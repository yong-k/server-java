package kr.hhplus.be.server.reservation.scheduler;

import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationTokenScheduler {

    private final ReservationTokenRepository reservationTokenRepository;
    private final ReservationTokenProperties reservationTokenProperties;
    private final RetryTemplate retryTemplate;

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
}
