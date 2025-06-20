package kr.hhplus.be.server.point;

import kr.hhplus.be.server.point.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PointRepository extends JpaRepository<PointHistory, Integer> {

    List<PointHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("""
        UPDATE User u
        SET u.point = u.point + :amount
        WHERE u.id = :userId
          AND u.point + :amount <= :maxPoint  
    """)
    int addPoint(@Param("userId") UUID userId, @Param("amount") int amount, @Param("maxPoint") int maxPoint);

    @Modifying
    @Query("""
        UPDATE User u
        SET u.point = u.point - :amount
        WHERE u.id = :userId
          AND u.point >= :amount
    """)
    int usePoint(@Param("userId") UUID userId, @Param("amount") int amount);
}
