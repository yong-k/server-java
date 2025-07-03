package kr.hhplus.be.server.reservation.application.port.out;

import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationTokenRepository {
    ReservationToken save(ReservationToken token);

    Optional<ReservationToken> findByIdAndStatus(UUID tokenId, ReservationTokenStatus status);

    List<ReservationToken> findByStatusAndExpiredAtBefore(ReservationTokenStatus status, LocalDateTime expiredAt);

    void deleteAll();
}
