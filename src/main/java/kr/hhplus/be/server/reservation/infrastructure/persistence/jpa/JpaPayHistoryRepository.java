package kr.hhplus.be.server.reservation.infrastructure.persistence.jpa;

import kr.hhplus.be.server.reservation.domain.PayHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPayHistoryRepository extends JpaRepository<PayHistory, Integer> {
}


