package kr.hhplus.be.server.point;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.point.domain.PointHistory;
import kr.hhplus.be.server.point.domain.TransactionType;
import kr.hhplus.be.server.point.domain.UserPoint;
import kr.hhplus.be.server.point.dto.PointHistoryRespDto;
import kr.hhplus.be.server.point.dto.PointRespDto;
import kr.hhplus.be.server.user.UserRepository;
import kr.hhplus.be.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    public List<PointHistoryRespDto> getPointHistories(UUID userId) {
        return pointRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PointHistoryRespDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PointRespDto chargePoint(UUID userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("사용자가 존재하지 않습니다: userId = " + userId));

        UserPoint userPoint = user.toUserPoint();
        user.updatePoint(userPoint.charge(amount));     // Dirty Checking OK

        PointHistory pointHistory = pointRepository.save(PointHistory.builder()
                .userId(userId)
                .type(TransactionType.CHARGE)
                .amount(amount)
                .currentPoint(user.getPoint())
                .build());

        return PointRespDto.from(pointHistory);
    }

    @Transactional
    public PointRespDto usePoint(UUID userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("사용자가 존재하지 않습니다: userId = " + userId));

        UserPoint userPoint = user.toUserPoint();
        user.updatePoint(userPoint.use(amount));    // Dirty Checking OK

        PointHistory pointHistory = pointRepository.save(PointHistory.builder()
                .userId(userId)
                .type(TransactionType.USE)
                .amount(amount)
                .currentPoint(user.getPoint())
                .build());

        return PointRespDto.from(pointHistory);
    }
}
