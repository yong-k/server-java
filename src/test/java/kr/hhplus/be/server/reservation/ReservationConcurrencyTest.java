package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.repository.ConcertRepository;
import kr.hhplus.be.server.concert.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.application.service.ReservationService;
import kr.hhplus.be.server.reservation.application.validator.ReservationTokenValidator;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import kr.hhplus.be.server.reservation.dto.PaymentReqDto;
import kr.hhplus.be.server.reservation.dto.PaymentRespDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationReqDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationRespDto;
import kr.hhplus.be.server.user.UserRepository;
import kr.hhplus.be.server.user.domain.User;
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
    private ReservationTokenValidator tokenValidator;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTokenRepository reservationTokenRepository;
    @Autowired
    private UserRepository userRepository;

    private Seat seat;
    private UUID allowedTokenId;
    private ConcertSchedule schedule;

    @BeforeEach
    void setup() {
        seatRepository.deleteAll();
        concertScheduleRepository.deleteAll();
        concertRepository.deleteAll();

        Concert concert = concertRepository.save(new Concert(null, "concert_A"));
        schedule = concertScheduleRepository.save(new ConcertSchedule(null, concert, LocalDateTime.now().plusDays(3)));
        seat = seatRepository.save(new Seat(null, schedule, 1, 100, null, SeatStatus.AVAILABLE, null, null));

        // ALLOWED 상태의 대기열토큰 생성 (테스트용)
        allowedTokenId = UUID.randomUUID();
        reservationTokenRepository.save(ReservationToken.builder()
                .id(allowedTokenId)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.ALLOWED)
                .build());
    }

    @Test
    void 동시에_같은_좌석_예약_시도() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<SeatReservationRespDto> successList = new ArrayList<>();

        doNothing().when(tokenValidator).validateToken(any());

        for (int i = 0; i < threadCount; i++) {
            UUID userId = UUID.randomUUID();
            executor.submit(() -> {
                try {
                    SeatReservationReqDto reqDto = new SeatReservationReqDto(seat.getId(), userId);
                    SeatReservationRespDto respDto = reservationService.reserveSeat(allowedTokenId, reqDto);
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

    @Test
    void 동시에_같은_좌석_결제_시도() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<PaymentRespDto> successList = new ArrayList<>();

        doNothing().when(tokenValidator).validateToken(any());

        // 임시예약 상태의 사용자 및 좌석 구성
        UUID reservedUserId = UUID.randomUUID();
        User user = userRepository.save(new User(reservedUserId, 1_000_000));
        seat.setUserId(reservedUserId);
        seat.setStatus(SeatStatus.TEMP_RESERVED);
        seat.setReleasedAt(LocalDateTime.now().plusMinutes(5)); // 임시배정 유효 시간
        seat = seatRepository.save(seat);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    PaymentReqDto reqDto = new PaymentReqDto(seat.getId(), reservedUserId);
                    PaymentRespDto respDto = reservationService.pay(allowedTokenId, reqDto);
                    successList.add(respDto);
                } catch (Exception e) {
                    log.debug("결제 실패: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 동시에 같은 좌석 결제안되면 성공개수는 1이여야 함.
        assertThat(successList.size()).isEqualTo(1);
        log.info("성공 개수: {}", successList.size());
    }
}
