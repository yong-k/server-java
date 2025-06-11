package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.point.PointService;
import kr.hhplus.be.server.reservation.dto.PaymentReqDto;
import kr.hhplus.be.server.reservation.dto.PaymentRespDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationReqDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationRespDto;
import kr.hhplus.be.server.reservation.exception.InvalidSeatStatusException;
import kr.hhplus.be.server.reservation.exception.InvalidSeatUserStatusException;
import kr.hhplus.be.server.user.UserRepository;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PayHistoryRepository payHistoryRepository;

    @Mock
    private PointService pointService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void 좌석_예약_정상() {
        // given
        int seatId = 1;
        UUID userId = UUID.randomUUID();
        Seat seat = Seat.builder()
                .id(seatId)
                .status(SeatStatus.AVAILABLE)
                .build();
        Seat reservedSeat = Seat.builder()
                .id(seatId)
                .userId(userId)
                .status(SeatStatus.TEMP_RESERVED)
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any())).thenReturn(reservedSeat);

        SeatReservationReqDto dto = new SeatReservationReqDto(seatId, userId);

        // when
        SeatReservationRespDto actual = reservationService.reserveSeat(dto);

        // then
        assertThat(actual.getSeatId()).isEqualTo(seatId);
        assertThat(actual.getUserId()).isEqualTo(userId);
        assertThat(actual.getStatus()).isEqualTo(SeatStatus.TEMP_RESERVED);
    }

    @Test
    void 좌석_예약_실패_예약불가좌석() {
        // given
        int seatId = 1;
        UUID userId = UUID.randomUUID();
        Seat seat = Seat.builder()
                .id(seatId)
                .status(SeatStatus.RESERVED)
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        SeatReservationReqDto dto = new SeatReservationReqDto(seatId, userId);

        // when
        // then
        assertThatThrownBy(() -> reservationService.reserveSeat(dto))
                .isInstanceOf(InvalidSeatStatusException.class);
    }

    @Test
    void 결제_성공() {
        // given
        int seatId = 1;
        UUID userId = UUID.randomUUID();
        int price = 50000;

        User user = new User(userId, 100000);
        Concert concert = Concert.builder().id(1).name("test_concert").build();
        ConcertSchedule schedule = ConcertSchedule.builder()
                .id(1)
                .concert(concert)
                .scheduleAt(LocalDateTime.now().plusDays(5))
                .build();
        Seat seat = Seat.builder()
                .id(seatId)
                .userId(userId)
                .status(SeatStatus.TEMP_RESERVED)
                .price(price)
                .concertSchedule(schedule)
                .build();

        PaymentReqDto dto = new PaymentReqDto(seatId, userId);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        PaymentRespDto actual = reservationService.pay(dto);

        // then
        assertThat(actual.getSeatId()).isEqualTo(seatId);
        assertThat(actual.getUserId()).isEqualTo(userId);
        assertThat(actual.getAmount()).isEqualTo(price);
    }

    @Test
    void 결제_실패_좌석상태_TEMP_RESERVED_아님() {
        // given
        int seatId = 1;
        UUID userId = UUID.randomUUID();

        User user = new User(userId, 100000);
        ConcertSchedule schedule = ConcertSchedule.builder()
                .id(1)
                .concert(mock(Concert.class))
                .scheduleAt(LocalDateTime.now().plusDays(5))
                .build();
        Seat seat = Seat.builder()
                .id(seatId)
                .userId(userId)
                .status(SeatStatus.HOLD)
                .price(10000)
                .concertSchedule(schedule)
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PaymentReqDto dto = new PaymentReqDto(seatId, userId);

        // when
        // then
        assertThatThrownBy(() -> reservationService.pay(dto))
                .isInstanceOf(InvalidSeatStatusException.class);

    }

    @Test
    void 결제_실패_다른사용자의_좌석() {
        // given
        int seatId = 1;
        UUID seatOwnerId = UUID.randomUUID();
        UUID reqUserId = UUID.randomUUID();

        User user = new User(reqUserId, 100000);
        ConcertSchedule schedule = ConcertSchedule.builder()
                .id(1)
                .concert(mock(Concert.class))
                .scheduleAt(LocalDateTime.now().plusDays(5))
                .build();
        Seat seat = Seat.builder()
                .id(seatId)
                .userId(seatOwnerId)
                .status(SeatStatus.TEMP_RESERVED)
                .price(10000)
                .concertSchedule(schedule)
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(userRepository.findById(reqUserId)).thenReturn(Optional.of(user));


        PaymentReqDto dto = new PaymentReqDto(seatId, reqUserId);

        // when
        // then
        assertThatThrownBy(() -> reservationService.pay(dto))
                .isInstanceOf(InvalidSeatUserStatusException.class);

    }
}