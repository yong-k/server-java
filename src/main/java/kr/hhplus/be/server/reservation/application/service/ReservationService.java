package kr.hhplus.be.server.reservation.application.service;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.point.PointService;
import kr.hhplus.be.server.reservation.application.port.out.PayHistoryRepository;
import kr.hhplus.be.server.reservation.application.port.in.ReservationUseCase;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.application.validator.ReservationTokenValidator;
import kr.hhplus.be.server.reservation.domain.*;
import kr.hhplus.be.server.reservation.dto.*;
import kr.hhplus.be.server.reservation.exception.InvalidSeatStatusException;
import kr.hhplus.be.server.reservation.exception.InvalidSeatUserStatusException;
import kr.hhplus.be.server.reservation.infrastructure.external.SeatLockManager;
import kr.hhplus.be.server.user.UserRepository;
import kr.hhplus.be.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService implements ReservationUseCase {

    private final ReservationTokenRepository reservationTokenRepository;
    private final SeatRepository seatRepository;
    private final PayHistoryRepository payHistoryRepository;
    private final UserRepository userRepository;

    private final PointService pointService;

    private final ReservationTokenValidator reservationTokenValidator;
    private final SeatLockManager seatLockManager;

    /**
     * userId + concertId에 unique 조건 걸려있음
     * 즉, userId + concertId로 조회했을 때:
     * -토큰 없음 → 새로 발급
     * -토큰 있고 상태 READY → 기존 토큰 재사용
     * -토큰 있고 상태 EXPIRED → 새로 발급 (기존 토큰은 삭제)
     * */
    @Override
    public ReservationTokenRespDto issueToken(ReservationTokenReqDto dto) {
        // 1. 기존 READY 상태인 토큰 있는지 조회
        Optional<ReservationToken> token =
                reservationTokenRepository.findByUserIdAndConcertIdAndStatus(dto.getUserId(), dto.getConcertId(), ReservationTokenStatus.READY);

        // 2. 있으면 재사용
        if (token.isPresent())
            return ReservationTokenRespDto.from(token.get());

        // 3. 없으면 발급
        ReservationToken newToken = ReservationToken.builder()
                .id(UUID.randomUUID())
                .userId(dto.getUserId())
                .concertId(dto.getConcertId())
                .order(0)   // 나중에 order 필요시 로직 변경 필요 (사용자 많아서 예매페이지 진입전 대기해야될 때)
                .status(ReservationTokenStatus.READY)
                .build();

        newToken = reservationTokenRepository.save(newToken);
        return ReservationTokenRespDto.from(newToken);
    }

    @Override
    @Transactional
    public SeatReservationRespDto reserveSeat(SeatReservationReqDto dto) {
        int seatId = dto.getSeatId();
        UUID userId = dto.getUserId();

        seatLockManager.lockSeat(seatId);
        try {
            Seat seat = seatRepository.findByIdWithLock(seatId)
                    .orElseThrow(() -> new DataNotFoundException("좌석이 존재하지 않습니다: seatId = " + seatId));

            // 대기열토큰 상태 체크
            int concertId = seat.getConcertSchedule().getConcert().getId();
            reservationTokenValidator.validateToken(userId, concertId);

            seat.validateReservable();

            // 해당 사용자에게 좌석 임시배정
            seat.reserve(userId);   // Dirty Checking OK

            return SeatReservationRespDto.builder()
                    .seatId(seat.getId())
                    .userId(seat.getUserId())
                    .status(seat.getStatus())
                    .build();
        } finally {
            seatLockManager.unlockSeat(seatId);
        }
    }

    @Override
    @Transactional
    public PaymentRespDto pay(PaymentReqDto dto) {
        int seatId = dto.getSeatId();
        UUID userId = dto.getUserId();

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new DataNotFoundException("좌석이 존재하지 않습니다: seatId = " + seatId));

        // 대기열토큰 상태 체크
        int concertId = seat.getConcertSchedule().getConcert().getId();
        reservationTokenValidator.validateToken(userId, concertId);

        try {
            seat.validatePayable(userId);
        } catch(InvalidSeatStatusException e) {
            savePayHistory(seat, userId, PaymentStatus.FAILED, PaymentReason.INVALID_SEAT_STATUS);
            throw e;
        } catch(InvalidSeatUserStatusException e) {
            savePayHistory(seat, userId, PaymentStatus.FAILED, PaymentReason.INVALID_USER);
            throw e;
        }

        // 포인트 사용 (조회, 차감, 포인트내역 저장)
        int price = seat.getPrice();
        pointService.usePoint(userId, price);

        // 좌석 상태 변경
        seat.pay();     // Dirty Checking OK

        // 대기열토큰 만료 처리 (JPA dirty checking)
        reservationTokenRepository.findByUserIdAndConcertIdAndStatus(userId, concertId, ReservationTokenStatus.READY)
                .ifPresent(ReservationToken::expire);

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
