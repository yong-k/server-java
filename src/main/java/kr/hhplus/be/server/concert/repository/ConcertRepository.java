package kr.hhplus.be.server.concert.repository;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.dto.ConcertScheduleRespDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert, Integer> {

    @Query("""
        SELECT c
        FROM Concert c
        JOIN ConcertSchedule cs ON cs.concert = c
        WHERE cs.scheduleAt > CURRENT_TIMESTAMP
    """)
    List<Concert> findOngoingConcerts();
}
