package kr.hhplus.be.server.reservation.infrastructure.kafka;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.reservation.application.port.in.PayHistoryUseCase;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.PaymentStatus;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import kr.hhplus.be.server.reservation.dto.kafka.PaymentSuccessMessage;
import kr.hhplus.be.server.reservation.infrastructure.external.RedisReservationRankingManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSuccessKafkaConsumer {
    private final RedisReservationRankingManager redisReservationRankingManager;
    private final ReservationTokenRepository reservationTokenRepository;
    private final SeatRepository seatRepository;
    private final PayHistoryUseCase payHistoryUseCase;

    @Transactional
    @KafkaListener(topics = "payment_success", groupId = "pay-consumer-group")
    public void consume(PaymentSuccessMessage message) {

        // 1) 예매율 랭킹 Redis 갱신 (일간, 주간, 월간)
        int scheduleId = message.getConcertScheduleId();
        redisReservationRankingManager.updateDailyReservationRate(scheduleId);
        redisReservationRankingManager.updateWeeklyReservationRate(scheduleId);
        redisReservationRankingManager.updateMonthlyReservationRate(scheduleId);

        // 2) 대기열토큰 결제완료 처리
        reservationTokenRepository.findByIdAndStatus(message.getTokenId(), ReservationTokenStatus.ALLOWED)
                .ifPresent(token -> {
                    token.complete();
                    reservationTokenRepository.save(token);
                });

        // 3) 결제 성공 내역 저장
        Seat seat = seatRepository.findById(message.getSeatId())
                .orElseThrow(() -> new DataNotFoundException("좌석이 존재하지 않습니다: seatId = " + message.getSeatId()));

        payHistoryUseCase.saveSuccessHistory(seat, message.getUserId(), PaymentStatus.SUCCESS, null);
    }
}
