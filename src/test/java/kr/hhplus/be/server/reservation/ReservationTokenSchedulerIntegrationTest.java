package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.reservation.application.port.out.ReservationQueueStore;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import kr.hhplus.be.server.reservation.scheduler.ReservationTokenScheduler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
public class ReservationTokenSchedulerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationTokenRepository reservationTokenRepository;

    @Autowired
    private ReservationQueueStore reservationQueueStore;

    @Autowired
    private ReservationTokenScheduler reservationTokenScheduler;

    @Test
    void WAITING_토큰은_스케줄러_실행시_ALLOWED로_변경되고_큐에서_제거된다() {
        // given
        UUID waitingTokenId = UUID.randomUUID();
        ReservationToken waitingToken = ReservationToken.builder()
                .id(waitingTokenId)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.WAITING)
                .build();
        reservationTokenRepository.save(waitingToken);

        // WAITING 토큰을 Redis 큐에 넣음
        reservationQueueStore.addToQueueIfAbsent(waitingTokenId, System.currentTimeMillis());

        // when
        reservationTokenScheduler.allowWaitingTokens();

        // then
        ReservationToken updated = reservationTokenRepository.findById(waitingTokenId).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(ReservationTokenStatus.ALLOWED);
        assertThat(updated.getExpiredAt()).isNotNull();

        // ALLOWED되면 큐에서 제거되어야 함
        int position = reservationQueueStore.getQueuePosition(waitingTokenId);
        assertThat(position).isEqualTo(-1);
    }

    @Test
    void ALLOWED_토큰은_expiredAt_경과시_TIMEOUT으로_변경된다() {
        // given
        UUID allowedTokenId = UUID.randomUUID();
        ReservationToken allowedToken = ReservationToken.builder()
                .id(allowedTokenId)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.ALLOWED)
                .expiredAt(LocalDateTime.now().minusMinutes(1)) // 이미 만료된 상태
                .build();
        reservationTokenRepository.save(allowedToken);

        // when
        reservationTokenScheduler.timeoutAllowedTokens();

        // then
        ReservationToken updated = reservationTokenRepository.findById(allowedTokenId).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(ReservationTokenStatus.TIMEOUT);
    }
}
