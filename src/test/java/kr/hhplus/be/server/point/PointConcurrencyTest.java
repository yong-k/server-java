package kr.hhplus.be.server.point;

import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.point.dto.PointRespDto;
import kr.hhplus.be.server.user.UserRepository;
import kr.hhplus.be.server.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class PointConcurrencyTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointService pointService;

    @Test
    void 동시에_같은_유저_포인트_차감() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        int amount = 100;   // 100원 * threadCount(100) = 총 10_000
        int initialPoint = 5_000;
        User user = userRepository.save(new User(UUID.randomUUID(), initialPoint));
        UUID userId = user.getId();
        List<PointRespDto> successList = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
             executor.submit(() -> {
                try {
                    PointRespDto respDto = pointService.usePoint(userId, amount);
                    successList.add(respDto);
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
             });
        }

        latch.await();
        executor.shutdown();

        int successCnt = successList.size();
        int actualPoint = userRepository.findById(userId).orElseThrow().getPoint();
        int expectedPoint = initialPoint - successCnt * amount;

        log.info("차감 성공 건수 = {}", successCnt);
        assertThat(actualPoint).isEqualTo(expectedPoint);
    }

    @Test
    void 동시에_같은_유저_포인트_충전() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        int amount = 30_000;
        int initialPoint = 1_950_000;
        User user = userRepository.save(new User(UUID.randomUUID(), initialPoint));
        UUID userId = user.getId();
        List<PointRespDto> successList = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    PointRespDto respDto = pointService.chargePoint(userId, amount);
                    successList.add(respDto);
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        int successCnt = successList.size();
        int actualPoint = userRepository.findById(userId).orElseThrow().getPoint();
        int expectedPoint = initialPoint + successCnt * amount;

        log.info("충전 성공 건수 = {}", successCnt);
        assertThat(actualPoint).isEqualTo(expectedPoint);
    }
}
