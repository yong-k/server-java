package kr.hhplus.be.server.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import kr.hhplus.be.server.reservation.dto.PaymentReqDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationReqDto;
import kr.hhplus.be.server.user.UserRepository;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class ReservationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ReservationTokenRepository reservationTokenRepository;
    @Autowired
    private SeatStatusProperties seatStatusProperties;

    private UUID userId;
    private Concert concert;
    private ConcertSchedule schedule;
    private Seat seat;
    private UUID allowedTokenId;

    @BeforeEach
    void setup() {
        // 초기화
        seatRepository.deleteAll();
        concertScheduleRepository.deleteAll();
        concertRepository.deleteAll();
        reservationTokenRepository.deleteAll();
        userId = UUID.randomUUID();

        // 유저 생성
        userRepository.save(new User(userId, 1_000_000));

        // 콘서트 + 스케줄 + 좌석
        concert = concertRepository.save(new Concert(null, "concert_A"));
        schedule = concertScheduleRepository.save(new ConcertSchedule(null, concert, LocalDateTime.now().plusDays(3), 50));
        seat = seatRepository.save(new Seat(null, schedule, 1, 50_000, null, SeatStatus.AVAILABLE, null, null));

        // ALLOWED 상태의 대기열토큰 생성 (테스트용)
        allowedTokenId = UUID.randomUUID();
        reservationTokenRepository.save(ReservationToken.builder()
                .id(allowedTokenId)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.ALLOWED)
                .build());
    }

    @Test
    void 예약_성공() throws Exception {
        SeatReservationReqDto dto = new SeatReservationReqDto(seat.getId(), userId);

        mockMvc.perform(post("/api/v1/reservation")
                        .header("X-TOKEN-ID", allowedTokenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TEMP_RESERVED"))
                .andDo(print());
    }

    @Test
    void 예약_실패_이미예약됨() throws Exception {
        seat.reserve(userId, seatStatusProperties.getTempReservedToExpiredMinutes());
        seatRepository.save(seat);

        SeatReservationReqDto dto = new SeatReservationReqDto(seat.getId(), userId);

        mockMvc.perform(post("/api/v1/reservation")
                        .header("X-TOKEN-ID", allowedTokenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 결제_성공() throws Exception {
        seat.reserve(userId, seatStatusProperties.getTempReservedToExpiredMinutes());
        seatRepository.save(seat);

        PaymentReqDto dto = new PaymentReqDto(seat.getId(), userId);

        mockMvc.perform(post("/api/v1/payment")
                        .header("X-TOKEN-ID", allowedTokenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatStatus").value("RESERVED"))
                .andDo(print());
    }


    @Test
    void 결제_실패_포인트부족() throws Exception {
        userRepository.save(new User(userId, 10));
        seat.reserve(userId, seatStatusProperties.getTempReservedToExpiredMinutes());
        seatRepository.save(seat);

        PaymentReqDto dto = new PaymentReqDto(seat.getId(), userId);

        mockMvc.perform(post("/api/v1/payment")
                        .header("X-TOKEN-ID", allowedTokenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 결제_실패_TEMP_RESERVED_아님() throws Exception {
        Seat seat = seatRepository.save(new Seat(null, schedule, 1, 10000, userId, SeatStatus.RESERVED, null, LocalDateTime.now()));

        PaymentReqDto dto = new PaymentReqDto(seat.getId(), userId);

        mockMvc.perform(post("/api/v1/payment")
                        .header("X-TOKEN-ID", allowedTokenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 결제_실패_다른_유저의_좌석() throws Exception {
        UUID anotherUserId = UUID.randomUUID();
        Seat seat = seatRepository.save(new Seat(null, schedule, 2, 10000, anotherUserId, SeatStatus.TEMP_RESERVED, LocalDateTime.now().plusMinutes(5), null));

        PaymentReqDto dto = new PaymentReqDto(seat.getId(), userId);

        mockMvc.perform(post("/api/v1/payment")
                        .header("X-TOKEN-ID", allowedTokenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private static String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
