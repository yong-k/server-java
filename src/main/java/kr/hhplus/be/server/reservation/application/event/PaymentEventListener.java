package kr.hhplus.be.server.reservation.application.event;

import kr.hhplus.be.server.reservation.application.port.in.PayHistoryUseCase;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.PaymentStatus;
import kr.hhplus.be.server.reservation.domain.PaymentSuccessEvent;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import kr.hhplus.be.server.reservation.infrastructure.external.RedisReservationRankingManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final RedisReservationRankingManager redisReservationRankingManager;
    private final ReservationTokenRepository reservationTokenRepository;
    private final PayHistoryUseCase payHistoryUseCase;

    // 비동기로 이벤트 발행주체의 트랜잭션이 커밋된 후에 수행한다.
    // 1) 예매율 랭킹 Redis 갱신 (일간, 주간, 월간)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationRanking(PaymentSuccessEvent event) {
        int scheduleId = event.getConcertScheduleId();
        redisReservationRankingManager.updateDailyReservationRate(scheduleId);
        redisReservationRankingManager.updateWeeklyReservationRate(scheduleId);
        redisReservationRankingManager.updateMonthlyReservationRate(scheduleId);
    }

    // 2) 대기열토큰 결제완료 처리
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationToken(PaymentSuccessEvent event) {
        reservationTokenRepository.findByIdAndStatus(event.getTokenId(), ReservationTokenStatus.ALLOWED)
                .ifPresent(token -> {
                    token.complete();
                    reservationTokenRepository.save(token);
                });
    }

    // 3) 결제 성공 내역 저장
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePayHistory(PaymentSuccessEvent event) {
        payHistoryUseCase.saveSuccessHistory(event.getSeat(), event.getUserId(), PaymentStatus.SUCCESS, null);
    }

}
