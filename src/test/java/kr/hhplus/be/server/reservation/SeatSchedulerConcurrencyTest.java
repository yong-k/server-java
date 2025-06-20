package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.repository.ConcertRepository;
import kr.hhplus.be.server.concert.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.point.PointService;
import kr.hhplus.be.server.point.dto.PointRespDto;
import kr.hhplus.be.server.reservation.application.service.ReservationService;
import kr.hhplus.be.server.reservation.application.validator.ReservationTokenValidator;
import kr.hhplus.be.server.reservation.dto.PaymentReqDto;
import kr.hhplus.be.server.reservation.scheduler.SeatStatusScheduler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@AutoConfigureMockMvc
public class SeatSchedulerConcurrencyTest extends BaseIntegrationTest {

    @Autowired
    ConcertRepository concertRepository;
    @Autowired
    ConcertScheduleRepository concertScheduleRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SeatStatusScheduler scheduler;
    @Autowired
    private ReservationService reservationService;

    @MockitoBean
    private ReservationTokenValidator tokenValidator;
    @MockitoBean
    private PointService pointService;

    private Seat seat;
    private UUID userId;

    @BeforeEach
    void setup() {
        Concert concert = concertRepository.save(new Concert(null, "test"));
        ConcertSchedule schedule = concertScheduleRepository.save(new ConcertSchedule(null, concert, LocalDateTime.now().plusDays(1)));
        seat = seatRepository.save(new Seat(null, schedule, 1, 10_000, null, SeatStatus.AVAILABLE, null, null));

        userId = UUID.randomUUID();
        seat.reserve(userId);

        // releasedAt 을 과거로 돌려 스케줄러가 즉시 EXPIRED 처리하도록 함
        seat.setReleasedAt(LocalDateTime.now().minusMinutes(1));

        // flush 해서 DB에 반영
       seatRepository.saveAndFlush(seat);

        doNothing().when(tokenValidator).validateToken(any(), anyInt());
        doReturn(mock(PointRespDto.class)).when(pointService).usePoint(any(), anyInt());
    }

    @Test
    void 결제와_스케줄러_충돌_테스트() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        // 결제
        executor.submit(() -> {
            try {
                reservationService.pay(new PaymentReqDto(seat.getId(), userId));
                log.info("결제 성공");
            } catch (Exception e) {
                log.info("결제 실패");
            } finally {
                latch.countDown();
            }
        });

        // 스케줄러
        executor.submit(() -> {
            try {
                scheduler.updateSeatStatus();   // TEMP_RESERVED → EXPIRED
                log.info("스케줄러_동시성_테스트: 스케줄러 실행 완료");
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executor.shutdown();

        Seat finalSeat = seatRepository.findById(seat.getId()).orElseThrow();
        if (finalSeat.getStatus() == SeatStatus.RESERVED) {
            // 결제 먼저 처리
            assertThat(finalSeat.getReleasedAt()).isNull();
        } else {
            // 스케줄러 먼저 처리
            assertThat(finalSeat.getStatus()).isIn(SeatStatus.EXPIRED, SeatStatus.HOLD, SeatStatus.AVAILABLE);
        }
    }
}
