package kr.hhplus.be.server.reservation.application.service;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.point.PointService;
import kr.hhplus.be.server.reservation.application.port.in.PayHistoryUseCase;
import kr.hhplus.be.server.reservation.application.port.out.PayHistoryRepository;
import kr.hhplus.be.server.reservation.application.port.in.ReservationUseCase;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.application.validator.ReservationTokenValidator;
import kr.hhplus.be.server.reservation.domain.*;
import kr.hhplus.be.server.reservation.dto.*;
import kr.hhplus.be.server.reservation.exception.InvalidSeatStatusException;
import kr.hhplus.be.server.reservation.exception.InvalidSeatUserStatusException;
import kr.hhplus.be.server.user.UserRepository;
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
    private final PayHistoryUseCase payHistoryUseCase;

    private final ReservationTokenValidator reservationTokenValidator;

    /**
     * userId + concertId에 unique 조건 걸려있음
     * 즉, userId + concertId로 조회했을 때:
     * -토큰 없음 → 새로 발급
     * -토큰 있고 상태 READY → 기존 토큰 재사용
     * -토큰 있고 상태 EXPIRED → 새로 발급 (기존 토큰은 삭제)
     */
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

        // JVM 락은 사용하지않고 DB락만 사용하여 예약 가능 여부 확인 → 상태 변경 → 저장까지 원자적으로 실행
        Seat seat = seatRepository.findByIdForUpdate(seatId)
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

        // 예외 상황에서도 결제 실패 이력을 반드시 남기기 위한 REQUIRES_NEW 트랜잭션 분리 [payHistoryUseCase.savePayHistory()]
        try {
            seat.validatePayable(userId);
        } catch (InvalidSeatStatusException e) {
            payHistoryUseCase.saveFailedHistory(seat, userId, PaymentStatus.FAILED, PaymentReason.INVALID_SEAT_STATUS);
            throw e;
        } catch (InvalidSeatUserStatusException e) {
            payHistoryUseCase.saveFailedHistory(seat, userId, PaymentStatus.FAILED, PaymentReason.INVALID_USER);
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

        // 결제내역 저장 [여기는 예외 생기면 롤백되어야 함 => REQUIRED]
        payHistoryUseCase.saveSuccessHistory(seat, userId, PaymentStatus.SUCCESS, null);

        return PaymentRespDto.builder()
                .userId(userId)
                .amount(price)
                .seatId(seat.getId())
                .seatUserId(seat.getUserId())
                .seatStatus(seat.getStatus())
                .build();
    }
}
