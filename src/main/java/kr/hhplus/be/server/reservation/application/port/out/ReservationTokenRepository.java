package kr.hhplus.be.server.reservation.application.port.out;

import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;

import java.util.Optional;
import java.util.UUID;

public interface ReservationTokenRepository {
    ReservationToken save(ReservationToken token);

    Optional<ReservationToken> findById(UUID id);

    Optional<ReservationToken> findByUserIdAndConcertIdAndStatus(UUID userId, int concertId, ReservationTokenStatus status);

    void deleteAll();
}
