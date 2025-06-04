package kr.hhplus.be.server.point;

import kr.hhplus.be.server.point.domain.PointHistory;
import kr.hhplus.be.server.point.domain.PointPolicy;
import kr.hhplus.be.server.point.domain.TransactionType;
import kr.hhplus.be.server.point.dto.PointHistoryRespDto;
import kr.hhplus.be.server.point.dto.PointRespDto;
import kr.hhplus.be.server.point.exception.PointPolicyViolationException;
import kr.hhplus.be.server.user.UserRepository;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    void 포인트내역_조회() {
        // given
        UUID userId = UUID.randomUUID();

        List<PointHistory> expected = new ArrayList<>();
        expected.add(PointHistory.builder()
                .id(1)
                .userId(userId)
                .type(TransactionType.CHARGE)
                .amount(10000)
                .currentPoint(10000)
                .createdAt(LocalDateTime.now())
                .build());
        expected.add(PointHistory.builder()
                .id(2)
                .userId(userId)
                .type(TransactionType.USE)
                .amount(3000)
                .currentPoint(7000)
                .createdAt(LocalDateTime.now())
                .build());

        when(pointRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(expected);

        // when
        List<PointHistoryRespDto> actual = pointService.findByUserId(userId);

        // then
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getType()).isEqualTo(TransactionType.CHARGE);
        assertThat(actual.get(1).getType()).isEqualTo(TransactionType.USE);
    }

    @Test
    void 포인트_충전_성공() {
        // given
        UUID userId = UUID.randomUUID();
        int amount = 10000;
        User user = new User(userId, 0);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PointHistory history = PointHistory.builder()
                .id(1)
                .userId(userId)
                .type(TransactionType.CHARGE)
                .amount(amount)
                .currentPoint(10000)
                .createdAt(LocalDateTime.now())
                .build();
        when(pointRepository.save(any())).thenReturn(history);

        // when
        PointRespDto actual = pointService.charge(userId, amount);

        // then
        assertThat(actual.getAmount()).isEqualTo(10000);
        assertThat(actual.getType()).isEqualTo(TransactionType.CHARGE);
        assertThat(actual.getCurrentPoint()).isEqualTo(10000);
    }

    @Test
    void 포인트_충전_최대잔고초과() {
        // given
        UUID userId = UUID.randomUUID();
        int amount = 10000;
        User user = new User(userId, PointPolicy.MAX_POINT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        // then
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(PointPolicyViolationException.class);
    }

    @Test
    void 포인트_충전_음수() {
        // given
        UUID userId = UUID.randomUUID();
        int amount = -10000;
        User user = new User(userId, 0);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        // then
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(PointPolicyViolationException.class);
    }

    @Test
    void 포인트_사용_성공() {
        // given
        UUID userId = UUID.randomUUID();
        int amount = 10000;
        User user = new User(userId, 50000);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PointHistory history = PointHistory.builder()
                .id(1)
                .userId(userId)
                .type(TransactionType.USE)
                .amount(amount)
                .currentPoint(40000)
                .createdAt(LocalDateTime.now())
                .build();
        when(pointRepository.save(any())).thenReturn(history);

        // when
        PointRespDto actual = pointService.use(userId, amount);

        // then
        assertThat(actual.getAmount()).isEqualTo(10000);
        assertThat(actual.getType()).isEqualTo(TransactionType.USE);
        assertThat(actual.getCurrentPoint()).isEqualTo(40000);
    }

    @Test
    void 포인트_사용_포인트부족() {
        // given
        UUID userId = UUID.randomUUID();
        int amount = 10000;
        User user = new User(userId, 5000);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        // then
        assertThatThrownBy(() -> pointService.use(userId, amount))
                .isInstanceOf(PointPolicyViolationException.class);
    }

    @Test
    void 포인트_사용_음수() {
        // given
        UUID userId = UUID.randomUUID();
        int amount = -10000;
        User user = new User(userId, 0);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        // then
        assertThatThrownBy(() -> pointService.use(userId, amount))
                .isInstanceOf(PointPolicyViolationException.class);
    }
}