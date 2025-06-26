package kr.hhplus.be.server.reservation.application.service;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.point.PointService;
import kr.hhplus.be.server.reservation.application.port.in.PayHistoryUseCase;
import kr.hhplus.be.server.reservation.application.port.in.ReservationUseCase;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.application.validator.ReservationTokenValidator;
import kr.hhplus.be.server.reservation.config.SeatStatusProperties;
import kr.hhplus.be.server.reservation.domain.*;
import kr.hhplus.be.server.reservation.dto.*;
import kr.hhplus.be.server.reservation.exception.InvalidSeatStatusException;
import kr.hhplus.be.server.reservation.exception.InvalidSeatUserStatusException;
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

    private final PointService pointService;
    private final PayHistoryUseCase payHistoryUseCase;

    private final ReservationTokenValidator reservationTokenValidator;
    private final SeatStatusProperties seatStatusProperties;

    /**
     * 새로고침했을 경우, 대기순번 새로 부여
     * 기존 토큰 무시하고 새로 발급
     */
    @Override
    public ReservationTokenRespDto issueToken(UUID userId) {
        ReservationToken token = ReservationToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .order(0)   // 나중에 대기열구현 및 redis 도입하면서 변경 예정
                .status(ReservationTokenStatus.WAITING)
                .build();

        return ReservationTokenRespDto.from(reservationTokenRepository.save(token));
    }

    @Override
    @Transactional
    public SeatReservationRespDto reserveSeat(UUID tokenId, SeatReservationReqDto dto) {
        int seatId = dto.getSeatId();
        UUID userId = dto.getUserId();

        // JVM 락은 사용하지않고 DB락만 사용하여 예약 가능 여부 확인 → 상태 변경 → 저장까지 원자적으로 실행
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new DataNotFoundException("좌석이 존재하지 않습니다: seatId = " + seatId));


        // 대기열토큰 검증
        reservationTokenValidator.validateToken(tokenId);

        seat.validateReservable();

        // 해당 사용자에게 좌석 임시배정
        seat.reserve(userId, seatStatusProperties.getTempReservedToExpiredMinutes());   // Dirty Checking OK

        return SeatReservationRespDto.builder()
                .seatId(seat.getId())
                .userId(seat.getUserId())
                .status(seat.getStatus())
                .build();
    }

    @Override
    @Transactional
    public PaymentRespDto pay(UUID tokenId, PaymentReqDto dto) {
        int seatId = dto.getSeatId();
        UUID userId = dto.getUserId();

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new DataNotFoundException("좌석이 존재하지 않습니다: seatId = " + seatId));

        // 대기열토큰 검증
        reservationTokenValidator.validateToken(tokenId);

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
        reservationTokenRepository.findByIdAndStatus(tokenId, ReservationTokenStatus.ALLOWED)
                .ifPresent(ReservationToken::complete);

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
