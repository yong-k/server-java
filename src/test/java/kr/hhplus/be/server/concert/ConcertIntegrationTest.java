package kr.hhplus.be.server.concert;

import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.repository.ConcertRepository;
import kr.hhplus.be.server.concert.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.concert.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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

    private UUID testUserId;
    private Concert testConcert;
    private ConcertSchedule schedule1;
    private ConcertSchedule schedule2;

    @BeforeEach
    void setup() {
        seatRepository.deleteAll();
        concertScheduleRepository.deleteAll();
        concertRepository.deleteAll();

        testUserId = UUID.randomUUID();

        // 진행중 콘서트
        testConcert = concertRepository.save(new Concert(null, "concert_A"));

        // 콘서트 일정 2개
        schedule1 = concertScheduleRepository.save(new ConcertSchedule(null, testConcert, LocalDateTime.now().plusDays(3)));
        schedule2 = concertScheduleRepository.save(new ConcertSchedule(null, testConcert, LocalDateTime.now().plusDays(4)));

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
        mockMvc.perform(get("/api/v1/concerts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("concert_A"))
                .andDo(print());
    }

    @Test
    void 콘서트_스케줄_조회_성공() throws Exception {
        mockMvc.perform(get("/api/v1/concerts/{concertId}/schedules", testConcert.getId())
                        .header("X-USER-ID", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].remainingSeats").value(5))
                .andExpect(jsonPath("$[1].remainingSeats").value(0))
                .andDo(print());
    }

    @Test
    void 콘서트_좌석목록_조회_성공() throws Exception {
        // 1. 대기열 토큰 발급
        mockMvc.perform(post("/api/v1/reservation/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "userId": "%s",
                          "concertId": %d
                        }
                        """.formatted(testUserId.toString(), testConcert.getId())))
                .andExpect(status().isCreated());

        // 2. 좌석 목록 조회
        mockMvc.perform(get("/api/v1/schedules/{scheduleId}/seats", schedule1.getId())
                        .header("X-USER-ID", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(5))
                .andExpect(jsonPath("$[0].number").value(1))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andDo(print());
    }

    @Test
    void 콘서트_좌석목록_조회_실패_토큰없이_접근() throws Exception {
        UUID anotherUserId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/schedules/{scheduleId}/seats", schedule1.getId())
                        .header("X-USER-ID", anotherUserId))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}
