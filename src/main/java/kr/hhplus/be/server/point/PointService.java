package kr.hhplus.be.server.point;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.point.domain.PointHistory;
import kr.hhplus.be.server.point.domain.PointPolicy;
import kr.hhplus.be.server.point.domain.TransactionType;
import kr.hhplus.be.server.point.dto.PointHistoryRespDto;
import kr.hhplus.be.server.point.dto.PointRespDto;
import kr.hhplus.be.server.point.exception.PointPolicyViolationException;
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

        int current = user.getPoint();
        PointPolicy.validateCharge(current, amount);

        // 조건부 UPDATE
        int updated = pointRepository.addPoint(userId, amount, PointPolicy.MAX_POINT);
        if (updated == 0) {
            throw new PointPolicyViolationException("포인트충전 실패: 동시성 충돌 OR 최대 보유 한도 초과");
        }

        int updatePoint = userRepository.findById(userId).orElseThrow().getPoint();
        PointHistory pointHistory = pointRepository.save(PointHistory.builder()
                .userId(userId)
                .type(TransactionType.CHARGE)
                .amount(amount)
                .currentPoint(updatePoint)
                .build());

        return PointRespDto.from(pointHistory);
    }

    @Transactional
    public PointRespDto usePoint(UUID userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("사용자가 존재하지 않습니다: userId = " + userId));

        int current = user.getPoint();
        PointPolicy.validateUse(current, amount);

        // 조건부 UPDATE
        int updated = pointRepository.usePoint(userId, amount);
        if (updated == 0) {
            throw new PointPolicyViolationException("포인트차감 실패: 동시성 충돌 OR 포인트 부족");
        }

        int updatePoint = userRepository.findById(userId).orElseThrow().getPoint();
        PointHistory pointHistory = pointRepository.save(PointHistory.builder()
                .userId(userId)
                .type(TransactionType.USE)
                .amount(amount)
                .currentPoint(updatePoint)
                .build());

        return PointRespDto.from(pointHistory);
    }
}
