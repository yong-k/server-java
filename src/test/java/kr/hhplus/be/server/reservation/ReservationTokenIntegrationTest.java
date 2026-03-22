package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
public class ReservationTokenIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationTokenRepository reservationTokenRepository;

    private UUID testUserId;

    @BeforeEach
    void setup() {
        testUserId = UUID.randomUUID();
    }

    @Test
    void 대기열토큰_발급_성공_ALLOWED() throws Exception {
        mockMvc.perform(post("/api/v1/reservation/queue")
                        .header("X-USER-ID", testUserId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ALLOWED"))
                .andDo(print());
    }

    @Test
    void 대기열_토큰_발급_성공_WAITING() throws Exception {
        for (int i = 0; i < 1000; i++) {
            reservationTokenRepository.save(
                    ReservationToken.builder()
                        .id(UUID.randomUUID())
                        .userId(UUID.randomUUID())
                        .status(ReservationTokenStatus.ALLOWED)
                        .build()
            );
        }

        mockMvc.perform(post("/api/v1/reservation/queue")
                        .header("X-USER-ID", testUserId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andDo(print());
    }
}
