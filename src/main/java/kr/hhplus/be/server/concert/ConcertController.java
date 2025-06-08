package kr.hhplus.be.server.concert;

import kr.hhplus.be.server.concert.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    // 진행중인 콘서트 목록 조회
    @GetMapping("/concerts")
    public ResponseEntity<List<ConcertRespDto>> getOngoingConcerts() {
        List<ConcertRespDto> concerts = concertService.getOngoingConcerts();
        return ResponseEntity.ok(concerts);
    }

    // 선택한 콘서트의 전체 스케줄 목록 + 잔여좌석수
    @GetMapping("/concerts/{concertId}/schedules")
    public ResponseEntity<List<ConcertScheduleRespDto>> getSchedulesWithRemainingSeats(@PathVariable("concertId") int concertId) {
        List<ConcertScheduleRespDto> schedules = concertService.getSchedulesWithRemainingSeats(concertId);
        return ResponseEntity.ok(schedules);
    }

    // 선택 콘서트 스케줄의 좌석 목록
    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<List<SeatRespDto>> getSeats(@PathVariable("scheduleId") int scheduleId) {
        List<SeatRespDto> seats = concertService.getSeatsBySchedule(scheduleId);
        return ResponseEntity.ok(seats);
    }

    // 콘서트 insert
    @PostMapping("/concerts")
    public ResponseEntity<ConcertRespDto> createConcert(@RequestBody ConcertCreateReqDto dto) {
        ConcertRespDto concert = concertService.createConcert(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(concert);
    }

    // 콘서트일정 insert + 좌석 50개 insert
    @PostMapping("/concerts/schedules")
    public ResponseEntity<ConcertScheduleRespDto> createConcertScheduleWithSeats(@RequestBody ConcertScheduleCreateReqDto dto) {
        ConcertScheduleRespDto concertSchedule = concertService.createScheduleWithSeats(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(concertSchedule);
    }

}
