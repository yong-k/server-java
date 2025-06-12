package kr.hhplus.be.server.reservation.application.validator;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import kr.hhplus.be.server.reservation.exception.InvalidReservationTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReservationTokenValidator {

    private final ReservationTokenRepository reservationTokenRepository;

    public void validateToken(UUID userId, int concertId) {
        ReservationToken token = reservationTokenRepository.findByUserIdAndConcertIdAndStatus(userId, concertId, ReservationTokenStatus.READY)
                .orElseThrow(() -> new InvalidReservationTokenException("대기열토큰이 유효하지 않습니다: userId[" + userId + "], concertId[" + concertId + "]"));
    }
}
