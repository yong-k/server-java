package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.reservation.application.port.out.ReservationQueueStore;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@AutoConfigureMockMvc
public class RedisReservationQueueStoreIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ReservationTokenRepository reservationTokenRepository;

    @Autowired
    private ReservationQueueStore reservationQueueStore;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String QUEUE_KEY = "queue:reservation";

    @BeforeEach
    void clearQueue() {
        redisTemplate.delete(QUEUE_KEY);
    }

    @Test
    void getWaitingQueueTokenIds는_WAITING상태_토큰만_반환한다() {
        // given
        UUID waiting1 = UUID.randomUUID();
        UUID allowed = UUID.randomUUID();
        UUID waiting2 = UUID.randomUUID();

        reservationTokenRepository.save(ReservationToken.builder()
                .id(waiting1)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.WAITING)
                .build());

        reservationTokenRepository.save(ReservationToken.builder()
                .id(allowed)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.ALLOWED)
                .build());

        reservationTokenRepository.save(ReservationToken.builder()
                .id(waiting2)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.WAITING)
                .build());

        reservationQueueStore.addToQueueIfAbsent(waiting1, 1);
        reservationQueueStore.addToQueueIfAbsent(allowed, 2);
        reservationQueueStore.addToQueueIfAbsent(waiting2, 3);

        // when
        List<UUID> result = reservationQueueStore.getWaitingQueueTokenIds(10);

        // then
        assertThat(result).containsExactly(waiting1, waiting2);
    }

    @Test
    void getWaitingQueueTokenIds는_Redis순서를_유지한다() {
        // given
        UUID token1 = UUID.randomUUID();
        UUID token2 = UUID.randomUUID();
        UUID token3 = UUID.randomUUID();

        reservationTokenRepository.save(ReservationToken.builder()
                .id(token1)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.WAITING)
                .build());

        reservationTokenRepository.save(ReservationToken.builder()
                .id(token2)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.WAITING)
                .build());

        reservationTokenRepository.save(ReservationToken.builder()
                .id(token3)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.WAITING)
                .build());

        reservationQueueStore.addToQueueIfAbsent(token1, 1);
        reservationQueueStore.addToQueueIfAbsent(token2, 2);
        reservationQueueStore.addToQueueIfAbsent(token3, 3);

        // when
        List<UUID> result = reservationQueueStore.getWaitingQueueTokenIds(10);

        // then
        assertThat(result).containsExactly(token1, token2, token3);
    }

    @Test
    void removeFromQueue는_토큰을_대기열에서_제거한다() {
        // given
        UUID token1 = UUID.randomUUID();
        UUID token2 = UUID.randomUUID();

        reservationTokenRepository.save(ReservationToken.builder()
                .id(token1)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.WAITING)
                .build());

        reservationTokenRepository.save(ReservationToken.builder()
                .id(token2)
                .userId(UUID.randomUUID())
                .status(ReservationTokenStatus.WAITING)
                .build());

        reservationQueueStore.addToQueueIfAbsent(token1, 1);
        reservationQueueStore.addToQueueIfAbsent(token2, 2);

        // when
        reservationQueueStore.removeFromQueue(List.of(token1));

        // then
        int position1 = reservationQueueStore.getQueuePosition(token1);
        int position2 = reservationQueueStore.getQueuePosition(token2);

        assertThat(position1).isEqualTo(-1);    // 제거됨
        assertThat(position2).isEqualTo(1);     // 남아있음
    }
}
