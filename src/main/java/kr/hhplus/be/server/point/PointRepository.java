package kr.hhplus.be.server.point;

import kr.hhplus.be.server.point.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PointRepository extends JpaRepository<PointHistory, Integer> {

    List<PointHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
