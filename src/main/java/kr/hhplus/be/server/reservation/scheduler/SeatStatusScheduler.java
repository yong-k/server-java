package kr.hhplus.be.server.reservation.scheduler;

import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.reservation.config.SeatStatusProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatStatusScheduler {

    private final SeatRepository seatRepository;
    private final SeatStatusProperties seatStatusProperties;

    @Transactional
    @Scheduled(fixedDelayString = "#{@seatStatusProperties.schedulerIntervalMs}")
    public void updateSeatStatus() {
        // TEMP_RESERVED 상태에서 5분동안 결제되지 않음 → EXPIRED
        List<Seat> toExpire = seatRepository.findByStatusAndReleasedAtBefore(SeatStatus.TEMP_RESERVED, LocalDateTime.now());
        toExpire.forEach(seat -> {
            seat.expire(seatStatusProperties.getExpiredToHoldMinutes());
            log.info("좌석 만료 처리됨 - seatId: {}, status: {}", seat.getId(), seat.getStatus());
        });

        // EXPIRED 1분 후 → HOLD
        List<Seat> toHold = seatRepository.findByStatusAndReleasedAtBefore(SeatStatus.EXPIRED, LocalDateTime.now());
        toHold.forEach(seat -> seat.hold(seatStatusProperties.getHoldToAvailableMinutes()));

        // HOLD 3분 후 → AVAILABLE
        List<Seat> toRelease = seatRepository.findByStatusAndReleasedAtBefore(SeatStatus.HOLD, LocalDateTime.now());
        toRelease.forEach(Seat::release);
    }
}
