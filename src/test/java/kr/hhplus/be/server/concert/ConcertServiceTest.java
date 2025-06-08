package kr.hhplus.be.server.concert;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.dto.ConcertScheduleRespDto;
import kr.hhplus.be.server.concert.repository.ConcertRepository;
import kr.hhplus.be.server.concert.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private ConcertService concertService;

    @Test
    void 진행중인_콘서트_조회() {
        // given
        List<Concert> expected = List.of(
                new Concert(1, "concert_1"),
                new Concert(2, "concert_2")
        );
        when(concertRepository.findOngoingConcerts()).thenReturn(expected);

        // when
        List<Concert> actual = concertRepository.findOngoingConcerts();

        // then
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getName()).isEqualTo("concert_1");
    }

    @Test
    void 스케줄_잔여좌석_조회_성공() {
        // given
        int concertId = 1;
        List<ConcertScheduleRespDto> expected = List.of(
                new ConcertScheduleRespDto(1, LocalDateTime.of(2025, 6, 6, 18, 0), 30L),
                new ConcertScheduleRespDto(2, LocalDateTime.of(2025, 6, 8, 18, 0), 0L)
        );
        when(concertScheduleRepository.findSchedulesWithRemainingSeats(concertId)).thenReturn(expected);

        // when
        List<ConcertScheduleRespDto> actual = concertScheduleRepository.findSchedulesWithRemainingSeats(concertId);

        // then
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getRemainingSeats()).isEqualTo(30L);
        assertThat(actual.get(1).getRemainingSeats()).isEqualTo(0L);
    }

    @Test
    void 스케줄_없을때_조회() {
        // given
        int concertId = -1;
        when(concertScheduleRepository.findSchedulesWithRemainingSeats(concertId)).thenReturn(Collections.emptyList());

        // when
        List<ConcertScheduleRespDto> actual = concertScheduleRepository.findSchedulesWithRemainingSeats(concertId);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void 스케줄_좌석목록_조회() {
        // given
        Concert concert = Concert.builder()
                .id(1)
                .name("concert_01")
                .build();
        ConcertSchedule schedule = ConcertSchedule.builder()
                .id(1)
                .concert(concert)
                .scheduleAt(null)
                .build();

        List<Seat> expected = List.of(
                new Seat(1, schedule, 1, 10000, null, SeatStatus.AVAILABLE, null, null),
                new Seat(2, schedule, 2, 50000, null, SeatStatus.RESERVED, null, null)
        );
        when(seatRepository.findByConcertSchedule_IdOrderByNumberAsc(schedule.getId())).thenReturn(expected);

        // when
        List<Seat> actual = seatRepository.findByConcertSchedule_IdOrderByNumberAsc(schedule.getId());

        // then
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getPrice()).isEqualTo(10000);
        assertThat(actual.get(0).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(actual.get(1).getPrice()).isEqualTo(50000);
        assertThat(actual.get(1).getStatus()).isEqualTo(SeatStatus.RESERVED);
    }
}