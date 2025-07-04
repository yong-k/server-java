package kr.hhplus.be.server.concert;

import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.repository.ConcertRepository;
import kr.hhplus.be.server.concert.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.config.SeatStatusProperties;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
public class ConcertIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ReservationTokenRepository reservationTokenRepository;

    private UUID testUserId;
    private UUID testReservationTokenId;
    private Concert testConcert;
    private ConcertSchedule schedule1;
    private ConcertSchedule schedule2;

    @BeforeEach
    void setup() {
        seatRepository.deleteAll();
        concertScheduleRepository.deleteAll();
        concertRepository.deleteAll();

        testUserId = UUID.randomUUID();
        testReservationTokenId = UUID.randomUUID();

        // ALLOWED 상태의 대기열토큰 미리 저장 (검증 통과용)
        ReservationToken savedToken = reservationTokenRepository.save(ReservationToken.builder()
                .id(testReservationTokenId)
                .userId(testUserId)
                .status(ReservationTokenStatus.ALLOWED)
                .build());

        // 진행중 콘서트
        testConcert = concertRepository.save(new Concert(null, "concert_A"));

        // 콘서트 일정 2개
        schedule1 = concertScheduleRepository.save(new ConcertSchedule(null, testConcert, LocalDateTime.now().plusDays(3), 50));
        schedule2 = concertScheduleRepository.save(new ConcertSchedule(null, testConcert, LocalDateTime.now().plusDays(4), 50));

        // 스케줄1 : 좌석 5개 모두 AVAILABLE
        for (int i = 1; i <= 5; i++) {
            seatRepository.save(new Seat(null, schedule1, i, 10000, null, SeatStatus.AVAILABLE, null, null));
        }

        // 스케줄2 : 좌석 5개 모두 RESERVED
        for (int i = 1; i <= 4; i++) {
            seatRepository.save(new Seat(null, schedule2, i, 10000, UUID.randomUUID(), SeatStatus.RESERVED, null, LocalDateTime.now()));
        }
    }

    @Test
    void 진행중인_콘서트목록_조회_성공() throws Exception {
        mockMvc.perform(get("/api/v1/concerts")
                        .header("X-TOKEN-ID", testReservationTokenId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("concert_A"))
                .andDo(print());
    }

    @Test
    void 콘서트_스케줄_조회_성공() throws Exception {
        mockMvc.perform(get("/api/v1/concerts/{concertId}/schedules", testConcert.getId())
                        .header("X-TOKEN-ID", testReservationTokenId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].remainingSeats").value(5))
                .andExpect(jsonPath("$[1].remainingSeats").value(0))
                .andDo(print());
    }

    @Test
    void 콘서트_좌석목록_조회_성공() throws Exception {
        mockMvc.perform(get("/api/v1/schedules/{scheduleId}/seats", schedule1.getId())
                        .header("X-TOKEN-ID", testReservationTokenId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(5))
                .andExpect(jsonPath("$[0].number").value(1))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andDo(print());
    }

    @Test
    void 콘서트_좌석목록_조회_실패_토큰없이_접근() throws Exception {
        UUID anotherTokenId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/schedules/{scheduleId}/seats", schedule1.getId())
                        .header("X-TOKEN-ID", anotherTokenId))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}
