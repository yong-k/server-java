package kr.hhplus.be.server.point;

import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.point.domain.PointHistory;
import kr.hhplus.be.server.point.domain.TransactionType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class PointIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    private UUID testUserId;

    @BeforeEach
    void setup() {
        testUserId = UUID.randomUUID();
        User testUser = new User(testUserId, 1_000_000);
        userRepository.save(testUser);
    }


    @Test
    void 포인트조회_성공() throws Exception {
        PointHistory history = new PointHistory(null, testUserId, TransactionType.CHARGE, 10_000, 1_100_000, LocalDateTime.now());
        pointRepository.save(history);

        mockMvc.perform(get("/api/v1/users/{userId}/points/histories", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[0].currentPoint").value(1_100_000))
                .andDo(print());
    }

    @Test
    void 포인트충전_성공() throws Exception {
        int amount = 500_000;

        mockMvc.perform(post("/api/v1/users/{userId}/points/charge", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentPoint").value(1_500_000))
                .andDo(print());
    }

    @Test
    void 포인트충전_실패_1회최대금액초과() throws Exception {
        int amount = 2_000_001;

        mockMvc.perform(post("/api/v1/users/{userId}/points/charge", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 포인트충전_실패_음수금액() throws Exception {
        int amount = -1000;

        mockMvc.perform(post("/api/v1/users/{userId}/points/charge", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 포인트충전_실패_최대보유포인트초과() throws Exception {
        int amount = 1_100_000;

        mockMvc.perform(post("/api/v1/users/{userId}/points/charge", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 포인트사용_성공() throws Exception {
        int amount = 300_000;

        mockMvc.perform(post("/api/v1/users/{userId}/points/use", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPoint").value(700_000))
                .andDo(print());
    }

    @Test
    void 포인트사용_실패_잔액부족() throws Exception {
        int amount = 2_000_000;

        mockMvc.perform(post("/api/v1/users/{userId}/points/use", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 포인트사용_실패_음수요청() throws Exception {
        int amount = -10_000;

        mockMvc.perform(post("/api/v1/users/{userId}/points/use", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}
