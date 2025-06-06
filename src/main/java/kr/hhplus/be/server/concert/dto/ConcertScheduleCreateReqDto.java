package kr.hhplus.be.server.concert.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertScheduleCreateReqDto {
    private int concertId;
    private LocalDateTime scheduleAt;
}
