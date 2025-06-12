package kr.hhplus.be.server.reservation.infrastructure.persistence.jpa;

import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    public Optional<ReservationToken> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ReservationToken> findByUserIdAndConcertIdAndStatus(UUID userId, int concertId, ReservationTokenStatus status) {
        return jpaRepository.findByUserIdAndConcertIdAndStatus(userId, concertId, status);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
