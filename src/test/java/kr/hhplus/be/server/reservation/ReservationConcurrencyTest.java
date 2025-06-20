package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.repository.ConcertRepository;
import kr.hhplus.be.server.concert.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.reservation.application.service.ReservationService;
import kr.hhplus.be.server.reservation.application.validator.ReservationTokenValidator;
import kr.hhplus.be.server.reservation.dto.SeatReservationReqDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationRespDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@AutoConfigureMockMvc
public class ReservationConcurrencyTest extends BaseIntegrationTest {

    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;
    @Autowired
    private SeatRepository seatRepository;

    @MockitoBean
    ReservationTokenValidator tokenValidator;

    @Autowired
    private ReservationService reservationService;

    private Seat seat;

    @BeforeEach
    void setup() {
        seatRepository.deleteAll();
        concertScheduleRepository.deleteAll();
        concertRepository.deleteAll();

        Concert concert = concertRepository.save(new Concert(null, "concert_A"));
        ConcertSchedule schedule = concertScheduleRepository.save(new ConcertSchedule(null, concert, LocalDateTime.now().plusDays(3)));
        seat = seatRepository.save(new Seat(null, schedule, 1, 10000, null, SeatStatus.AVAILABLE, null, null));
    }

    @Test
    void 동시에_같은_좌석_예약_시도() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<SeatReservationRespDto> successList = new ArrayList<>();

        doNothing().when(tokenValidator).validateToken(any(), anyInt());

        for (int i = 0; i < threadCount; i++) {
            UUID userId = UUID.randomUUID();
            executor.submit(() -> {
                try {
                    SeatReservationReqDto reqDto = new SeatReservationReqDto(seat.getId(), userId);
                    SeatReservationRespDto respDto = reservationService.reserveSeat(reqDto);
                    successList.add(respDto);
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 동시에 같은 좌석 예약안되면 성공개수는 1이여야 함.
        assertThat(successList.size()).isEqualTo(1);
        log.info("성공 개수: {}", successList.size());
    }
}
