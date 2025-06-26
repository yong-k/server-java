package kr.hhplus.be.server.reservation.infrastructure.persistence.jpa;

import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaReservationTokenRepository extends JpaRepository<ReservationToken, UUID> {
    Optional<ReservationToken> findByIdAndStatus(UUID tokenId, ReservationTokenStatus status);

}
