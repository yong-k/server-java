package kr.hhplus.be.server.reservation.infrastructure.external;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class RedisReservationRankingManager {

    private final StringRedisTemplate redisTemplate;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;

    // 예매율을 계산하여 Redis에 갱신
    public void updateDailyReservationRate(int concertScheduleId) {
        ConcertSchedule schedule = concertScheduleRepository.findById(concertScheduleId)
                .orElseThrow(() -> new DataNotFoundException("콘서트 스케줄이 존재하지 않습니다: id = " + concertScheduleId));

        int totalSeats = schedule.getTotalSeats();
        if (totalSeats <= 0) return;

        // 예약완료(결제) 상태 좌석수
        int reservedSeats = seatRepository.countByConcertScheduleIdAndStatus(concertScheduleId, SeatStatus.RESERVED);

        // 예매율 계산
        double rate = (double) reservedSeats / totalSeats;

        // Redis에 갱신
        String key = "concert:ranking:daily:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);   // ex) concert:ranking:daily:20250705
        redisTemplate.opsForZSet().add(key, String.valueOf(concertScheduleId), rate);
        redisTemplate.expire(key, Duration.ofDays(1));
    }
}
