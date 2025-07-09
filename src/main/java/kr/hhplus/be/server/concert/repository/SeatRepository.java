package kr.hhplus.be.server.concert.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByConcertSchedule_IdOrderByNumberAsc(int concertScheduleId);

    List<Seat> findByStatusAndReleasedAtBefore(SeatStatus status, LocalDateTime dateTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :seatId")
    Optional<Seat> findByIdForUpdate(@Param("seatId") int seatId);

    int countByConcertScheduleIdAndStatus(int concertScheduleId, SeatStatus status);
}
