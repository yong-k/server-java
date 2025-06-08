package kr.hhplus.be.server.concert;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.dto.*;
import kr.hhplus.be.server.concert.repository.ConcertRepository;
import kr.hhplus.be.server.concert.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;

    public List<ConcertRespDto> getOngoingConcerts() {
        return concertRepository.findOngoingConcerts().stream()
                .map(ConcertRespDto::from)
                .collect(Collectors.toList());
    }

    public List<ConcertScheduleRespDto> getSchedulesWithRemainingSeats(int concertId) {
        return concertScheduleRepository.findSchedulesWithRemainingSeats(concertId);
    }

    public List<SeatRespDto> getSeatsBySchedule(int concertScheduleId) {
        return seatRepository.findByConcertSchedule_IdOrderByNumberAsc(concertScheduleId)
                .stream()
                .map(SeatRespDto::from)
                .collect(Collectors.toList());
    }

    public ConcertRespDto createConcert(ConcertCreateReqDto dto) {
        Concert concert = Concert.builder()
                .name(dto.getName())
                .build();

        Concert saved = concertRepository.save(concert);
        return ConcertRespDto.from(saved);
    }

    @Transactional
    public ConcertScheduleRespDto createScheduleWithSeats(ConcertScheduleCreateReqDto dto) {
        Concert concert = concertRepository.findById(dto.getConcertId())
                .orElseThrow(() -> new DataNotFoundException("콘서트가 존재하지 않습니다: concertId = " + dto.getConcertId()));

        // 콘서트 일정 생성
        ConcertSchedule schedule = concertScheduleRepository.save(ConcertSchedule.builder()
                .concert(concert)
                .scheduleAt(dto.getScheduleAt())
                .build());

        // 좌석 50개 생성
        List<Seat> seats = IntStream.rangeClosed(1, 50)
                .mapToObj(i -> Seat.builder()
                        .concertSchedule(schedule)
                        .number(i)
                        .price(50000)
                        .status(SeatStatus.AVAILABLE)
                        .build())
                .toList();
        seatRepository.saveAll(seats);

        return ConcertScheduleRespDto.builder()
                .concertId(concert.getId())
                .scheduleId(schedule.getId())
                .scheduleAt(schedule.getScheduleAt())
                .build();
    }
}
