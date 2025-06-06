package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.point.PointService;
import kr.hhplus.be.server.reservation.domain.PayHistory;
import kr.hhplus.be.server.reservation.domain.PaymentReason;
import kr.hhplus.be.server.reservation.domain.PaymentStatus;
import kr.hhplus.be.server.reservation.dto.PaymentReqDto;
import kr.hhplus.be.server.reservation.dto.PaymentRespDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationReqDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationRespDto;
import kr.hhplus.be.server.reservation.exception.SeatPaymentException;
import kr.hhplus.be.server.reservation.exception.SeatReservationException;
import kr.hhplus.be.server.user.UserRepository;
import kr.hhplus.be.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final SeatRepository seatRepository;
    private final PayHistoryRepository payHistoryRepository;
    private final UserRepository userRepository;

    private final PointService pointService;

    @Transactional
    public SeatReservationRespDto reserveSeat(SeatReservationReqDto dto) {
        int seatId = dto.getSeatId();
        UUID userId = dto.getUserId();

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatReservationException("좌석이 존재하지 않습니다: seatId = " + seatId));

        if (!seat.getStatus().equals(SeatStatus.AVAILABLE))
            throw new SeatReservationException("예약불가 좌석입니다: [seatId = " + seat.getId() + ", status = + " + seat.getStatus() + "]");

        // 해당 사용자에게 좌석 임시배정
        seat.reserve(userId);
        seat = seatRepository.save(seat);

        return SeatReservationRespDto.builder()
                .seatId(seat.getId())
                .userId(seat.getUserId())
                .status(seat.getStatus())
                .build();
    }

    @Transactional
    public PaymentRespDto pay(PaymentReqDto dto) {
        int seatId = dto.getSeatId();
        UUID userId = dto.getUserId();

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatReservationException("좌석이 존재하지 않습니다: seatId = " + seatId));

        if (!seat.getStatus().equals(SeatStatus.TEMP_RESERVED)) {
            savePayHistory(seat, userId, PaymentStatus.FAILED, PaymentReason.INVALID_SEAT_STATUS);
            throw new SeatPaymentException("결제불가 좌석입니다: [seatId = " + seat.getId() + ", status = " + seat.getStatus() + "]");
        }

        if (!seat.getUserId().equals(userId)) {
            savePayHistory(seat, userId, PaymentStatus.FAILED, PaymentReason.INVALID_USER);
            throw new SeatPaymentException("(결제불가)해당 사용자에게 배정된 좌석이 아닙니다: [seatId = " + seat.getId()
                    + "배정된userId = + " + seat.getStatus() + "현재userId = " + userId + "]");
        }

        // 포인트 사용 (조회, 차감, 포인트내역 저장)
        int price = seat.getPrice();
        pointService.usePoint(userId, price);

        // 좌석 상태 변경
        seat.pay();
        seatRepository.save(seat);

        // 결제내역 저장
        savePayHistory(seat, userId, PaymentStatus.SUCCESS, null);

        return PaymentRespDto.builder()
                .userId(userId)
                .amount(price)
                .seatId(seat.getId())
                .seatUserId(seat.getUserId())
                .seatStatus(seat.getStatus())
                .build();
    }

    private void savePayHistory(Seat seat, UUID userId, PaymentStatus status, PaymentReason reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("사용자가 존재하지 않습니다: userId = " + userId));
        ConcertSchedule schedule = seat.getConcertSchedule();
        Concert concert = schedule.getConcert();

        PayHistory history = PayHistory.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .concertId(concert.getId())
                .concertName(concert.getName())
                .concertScheduleId(schedule.getId())
                .scheduleAt(schedule.getScheduleAt())
                .seatId(seat.getId())
                .seatNumber(seat.getNumber())
                .seatPrice(seat.getPrice())
                .amount(seat.getPrice())   //--수정필요) 나중에 할인같은거 생기면, 실제 결제금액을 넣어야한다.
                .status(status)
                .reason(reason != null ? reason.getMessage() : null)
                .build();

        payHistoryRepository.save(history);
    }

}
