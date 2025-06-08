package kr.hhplus.be.server.concert.repository;

import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByConcertSchedule_IdOrderByNumberAsc(int concertScheduleId);

    List<Seat> findByStatusAndReleasedAtBefore(SeatStatus status, LocalDateTime dateTime);
}
