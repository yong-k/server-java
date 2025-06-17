package kr.hhplus.be.server.concert.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertScheduleCreateReqDto {

    @NotNull(message = "콘서트ID는 필수입니다")
    private Integer concertId;

    @NotNull(message = "콘서트일시는 필수입니다")
    private LocalDateTime scheduleAt;
}
