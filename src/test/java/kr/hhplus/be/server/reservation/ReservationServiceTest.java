package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.point.PointService;
import kr.hhplus.be.server.reservation.application.port.in.PayHistoryUseCase;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.application.service.ReservationService;
import kr.hhplus.be.server.reservation.application.validator.ReservationTokenValidator;
import kr.hhplus.be.server.reservation.config.SeatStatusProperties;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import kr.hhplus.be.server.reservation.dto.PaymentReqDto;
import kr.hhplus.be.server.reservation.dto.PaymentRespDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationReqDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationRespDto;
import kr.hhplus.be.server.reservation.exception.InvalidSeatStatusException;
import kr.hhplus.be.server.reservation.exception.InvalidSeatUserStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationTokenRepository reservationTokenRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private PointService pointService;
    @Mock
    private PayHistoryUseCase payHistoryUseCase;
    @Mock
    private ReservationTokenValidator reservationTokenValidator;
    @Mock
    private SeatStatusProperties seatStatusProperties;

    @InjectMocks
    private ReservationService reservationService;

    private UUID userId;
    private Concert concert;
    private ConcertSchedule schedule;
    private UUID allowedTokenId;
    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        concert = Concert.builder().id(1).name("test_concert").build();
        schedule = ConcertSchedule.builder()
                .id(1)
                .concert(concert)
                .scheduleAt(LocalDateTime.now().plusDays(5))
                .build();

        // ALLOWED 상태의 대기열토큰 생성 (테스트용)
        allowedTokenId = UUID.randomUUID();
        reservationTokenRepository.save(ReservationToken.builder()
                .id(allowedTokenId)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.ALLOWED)
                .build());
    }

    @Test
    void 좌석_예약_정상() {
        // given
        int seatId = 1;
        Seat seat = Seat.builder()
                .id(seatId)
                .status(SeatStatus.AVAILABLE)
                .concertSchedule(schedule)
                .build();

        when(seatRepository.findByIdForUpdate(seatId)).thenReturn(Optional.of(seat));

        SeatReservationReqDto dto = new SeatReservationReqDto(seatId, userId);

        // when
        SeatReservationRespDto actual = reservationService.reserveSeat(allowedTokenId, dto);

        // then
        assertThat(actual.getSeatId()).isEqualTo(seatId);
        assertThat(actual.getUserId()).isEqualTo(userId);
        assertThat(actual.getStatus()).isEqualTo(SeatStatus.TEMP_RESERVED);
    }

    @Test
    void 좌석_예약_실패_예약불가좌석() {
        // given
        int seatId = 1;
        Seat seat = Seat.builder()
                .id(seatId)
                .status(SeatStatus.RESERVED)
                .concertSchedule(schedule)
                .build();

        when(seatRepository.findByIdForUpdate(seatId)).thenReturn(Optional.of(seat));

        SeatReservationReqDto dto = new SeatReservationReqDto(seatId, userId);

        // when
        // then
        assertThatThrownBy(() -> reservationService.reserveSeat(allowedTokenId, dto))
                .isInstanceOf(InvalidSeatStatusException.class);
    }

    @Test
    void 결제_성공() {
        // given
        int seatId = 1;
        int price = 50000;
        Seat seat = Seat.builder()
                .id(seatId)
                .userId(userId)
                .status(SeatStatus.TEMP_RESERVED)
                .price(price)
                .concertSchedule(schedule)
                .build();

        PaymentReqDto dto = new PaymentReqDto(seatId, userId);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        // when
        PaymentRespDto actual = reservationService.pay(allowedTokenId, dto);

        // then
        assertThat(actual.getSeatId()).isEqualTo(seatId);
        assertThat(actual.getUserId()).isEqualTo(userId);
        assertThat(actual.getAmount()).isEqualTo(price);
    }

    @Test
    void 결제_실패_좌석상태_TEMP_RESERVED_아님() {
        // given
        int seatId = 1;
        Seat seat = Seat.builder()
                .id(seatId)
                .userId(userId)
                .status(SeatStatus.HOLD)
                .price(10000)
                .concertSchedule(schedule)
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        PaymentReqDto dto = new PaymentReqDto(seatId, userId);

        // when
        // then
        assertThatThrownBy(() -> reservationService.pay(allowedTokenId, dto))
                .isInstanceOf(InvalidSeatStatusException.class);

    }

    @Test
    void 결제_실패_다른사용자의_좌석() {
        // given
        int seatId = 1;
        UUID seatOwnerId = UUID.randomUUID();
        UUID reqUserId = UUID.randomUUID();
        Seat seat = Seat.builder()
                .id(seatId)
                .userId(seatOwnerId)
                .status(SeatStatus.TEMP_RESERVED)
                .price(10000)
                .concertSchedule(schedule)
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        PaymentReqDto dto = new PaymentReqDto(seatId, reqUserId);

        // when
        // then
        assertThatThrownBy(() -> reservationService.pay(allowedTokenId, dto))
                .isInstanceOf(InvalidSeatUserStatusException.class);

    }
}