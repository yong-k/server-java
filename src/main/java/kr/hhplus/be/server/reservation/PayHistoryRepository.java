package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.reservation.domain.PayHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayHistoryRepository extends JpaRepository<PayHistory, Integer> {
}
