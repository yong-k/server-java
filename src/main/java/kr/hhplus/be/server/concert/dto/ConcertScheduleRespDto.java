package kr.hhplus.be.server.concert.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConcertScheduleRespDto {
    private int concertId;
    private int scheduleId;
    private LocalDateTime scheduleAt;
    private Long remainingSeats;
    private Integer totalSeats;

    public ConcertScheduleRespDto(int scheduleId, LocalDateTime scheduleAt, Long remainingSeats, Integer totalSeats) {
        this.scheduleId = scheduleId;
        this.scheduleAt = scheduleAt;
        this.remainingSeats = remainingSeats;
        this.totalSeats = totalSeats;
    }

    public ConcertScheduleRespDto(int concertId, int scheduleId, LocalDateTime scheduleAt, Long remainingSeats) {
        this.concertId = concertId;
        this.scheduleId = scheduleId;
        this.scheduleAt = scheduleAt;
        this.remainingSeats = remainingSeats;
    }
}
