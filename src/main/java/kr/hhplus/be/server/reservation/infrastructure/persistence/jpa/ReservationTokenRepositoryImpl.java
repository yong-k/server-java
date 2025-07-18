package kr.hhplus.be.server.reservation.infrastructure.persistence.jpa;

import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReservationTokenRepositoryImpl implements ReservationTokenRepository {

    private final JpaReservationTokenRepository jpaRepository;

    @Override
    public ReservationToken save(ReservationToken token) {
        return jpaRepository.save(token);
    }

    @Override
    public Optional<ReservationToken> findById(UUID tokenId) {
        return jpaRepository.findById(tokenId);
    }

    @Override
    public Optional<ReservationToken> findByIdAndStatus(UUID tokenId, ReservationTokenStatus status) {
        return jpaRepository.findByIdAndStatus(tokenId, status);
    }

    @Override
    public List<ReservationToken> findByStatusAndExpiredAtBefore(ReservationTokenStatus status, LocalDateTime expiredAt) {
        return jpaRepository.findByStatusAndExpiredAtBefore(status, expiredAt);
    }

    @Override
    public List<ReservationToken> findAllById(List<UUID> tokenIds) {
        return jpaRepository.findAllById(tokenIds);
    }

    @Override
    public long countByStatus(ReservationTokenStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
