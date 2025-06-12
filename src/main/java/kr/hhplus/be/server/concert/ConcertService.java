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
import kr.hhplus.be.server.reservation.application.port.in.ReservationUseCase;
import kr.hhplus.be.server.reservation.application.validator.ReservationTokenValidator;
import kr.hhplus.be.server.reservation.dto.ReservationTokenReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;

    private final ReservationUseCase reservationUseCase;
    private final ReservationTokenValidator reservationTokenValidator;

    public List<ConcertRespDto> getOngoingConcerts() {
        return concertRepository.findOngoingConcerts().stream()
                .map(ConcertRespDto::from)
                .collect(Collectors.toList());
    }

    // 콘서트 선택 후, 해당 콘서트일정 조회하면서 ──▷ 대기열토큰 발급
    public List<ConcertScheduleRespDto> getSchedulesWithRemainingSeats(UUID userId, int concertId) {
        // 대기열토큰 발급
        reservationUseCase.issueToken(new ReservationTokenReqDto(userId, concertId));
        return concertScheduleRepository.findSchedulesWithRemainingSeats(concertId);
    }

    public List<SeatRespDto> getSeatsBySchedule(UUID userId, int concertScheduleId) {
        // 대기열토큰 검증
        ConcertSchedule schedule = concertScheduleRepository.findById(concertScheduleId)
                .orElseThrow(() -> new DataNotFoundException("콘서트일정이 존재하지 않습니다: concertScheduleId = " + concertScheduleId));
        int concertId = schedule.getConcert().getId();
        reservationTokenValidator.validateToken(userId, concertId);

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
