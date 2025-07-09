package kr.hhplus.be.server.concert.repository;

import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.dto.ConcertScheduleRespDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Integer> {

    @Query("""
        SELECT new kr.hhplus.be.server.concert.dto.ConcertScheduleRespDto(
            cs.id, cs.scheduleAt, COUNT(CASE WHEN s.status = 'AVAILABLE' THEN 1 END), cs.totalSeats
        )
        FROM ConcertSchedule cs JOIN Seat s 
            ON s.concertSchedule.id = cs.id
        WHERE cs.concert.id = :concertId
        GROUP BY cs.id, cs.scheduleAt
        ORDER BY cs.scheduleAt
    """)
    List<ConcertScheduleRespDto> findSchedulesWithRemainingSeats(@Param("concertId") int concertId);
}
