package kr.hhplus.be.server.reservation.application.service;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.point.PointService;
import kr.hhplus.be.server.reservation.application.event.PaymentEventPublisher;
import kr.hhplus.be.server.reservation.application.port.in.PayHistoryUseCase;
import kr.hhplus.be.server.reservation.application.port.in.ReservationUseCase;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.application.validator.ReservationTokenValidator;
import kr.hhplus.be.server.reservation.config.SeatStatusProperties;
import kr.hhplus.be.server.reservation.domain.*;
import kr.hhplus.be.server.reservation.dto.*;
import kr.hhplus.be.server.reservation.exception.InvalidSeatStatusException;
import kr.hhplus.be.server.reservation.exception.InvalidSeatUserStatusException;
import kr.hhplus.be.server.reservation.exception.RedisDistributedLockException;
import kr.hhplus.be.server.reservation.infrastructure.external.RedisDistributedLockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private final RedisDistributedLockManager redisDistributedLockManager;

    private final PaymentEventPublisher paymentEventPublisher;

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
                .status(ReservationTokenStatus.ALLOWED)     // 대기열 구현 후, WAITING으로 변경하기
//                .status(ReservationTokenStatus.WAITING)
                .build();

        return ReservationTokenRespDto.from(reservationTokenRepository.save(token));
    }

    @Override
    @Transactional
    public SeatReservationRespDto reserveSeat(UUID tokenId, SeatReservationReqDto dto) {
        int seatId = dto.getSeatId();
        UUID userId = dto.getUserId();

        String lockKey = "lock:seat:" + seatId;
        String lockValue = redisDistributedLockManager.generateUniqueValue();
        Duration expire = Duration.ofSeconds(3);    // 락의 만료 시간(TTL)

        int maxAttempts = 3;
        int attempt = 0;
        long retryDelayMs = 100;    // 락 재시도 대기시간
        while (true) {
            boolean locked = redisDistributedLockManager.lock(lockKey, lockValue, expire);
            if (locked) {
                try {
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
                } finally {
                    redisDistributedLockManager.unlock(lockKey, lockValue);
                }
            }

            attempt++;
            if (attempt >= maxAttempts) {
                throw new RedisDistributedLockException("Redis 락 획득 실패 - 최대 재시도 초과 (lockKey=" + lockKey + ", attempts=" + attempt + ")");
            }

            try {
                Thread.sleep(retryDelayMs);     // 대기했다가 락 획득 재시도
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RedisDistributedLockException("Redis 락 대기 중 인터럽트 발생 (lockKey=" + lockKey + ", attempts=" + attempt + ")", e);
            }
        }
    }

    @Override
    @Transactional
    public PaymentRespDto pay(UUID tokenId, PaymentReqDto dto) {
        int seatId = dto.getSeatId();
        UUID userId = dto.getUserId();

        String lockKey = "lock:seat:" + seatId;
        String lockValue = redisDistributedLockManager.generateUniqueValue();
        Duration expire = Duration.ofSeconds(3);

        int maxAttempts = 3;
        int attempt = 0;
        long retryDelayMs = 100;
        while (true) {
            boolean locked = redisDistributedLockManager.lock(lockKey, lockValue, expire);
            if (locked) {
                try {
                    Seat seat = seatRepository.findByIdForUpdate(seatId)
                            .orElseThrow(() -> new DataNotFoundException("좌석이 존재하지 않습니다: seatId = " + seatId));

                    // 대기열토큰 검증
                    reservationTokenValidator.validateToken(tokenId);

                    // 좌석 결제 가능 여부 검증
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

                    // 결제 성공 이벤트 발행
                    paymentEventPublisher.success(new PaymentSuccessEvent(seat.getConcertSchedule().getId(), userId, tokenId, seat));

                    return PaymentRespDto.builder()
                            .userId(userId)
                            .amount(price)
                            .seatId(seat.getId())
                            .seatUserId(seat.getUserId())
                            .seatStatus(seat.getStatus())
                            .build();
                } finally {
                    redisDistributedLockManager.unlock(lockKey, lockValue);
                }
            }

            attempt++;
            if (attempt >= maxAttempts) {
                throw new RedisDistributedLockException("Redis 락 획득 실패 - 최대 재시도 초과 (lockKey=" + lockKey + ", attempts=" + attempt + ")");
            }

            try {
                Thread.sleep(retryDelayMs);     // 대기했다가 락 획득 재시도
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RedisDistributedLockException("Redis 락 대기 중 인터럽트 발생 (lockKey=" + lockKey + ", attempts=" + attempt + ")", e);
            }
        }
    }
}
