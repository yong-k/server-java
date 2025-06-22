package kr.hhplus.be.server.reservation.application.service;

import kr.hhplus.be.server.common.exception.DataNotFoundException;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.reservation.application.port.in.PayHistoryUseCase;
import kr.hhplus.be.server.reservation.application.port.out.PayHistoryRepository;
import kr.hhplus.be.server.reservation.domain.PayHistory;
import kr.hhplus.be.server.reservation.domain.PaymentReason;
import kr.hhplus.be.server.reservation.domain.PaymentStatus;
import kr.hhplus.be.server.user.UserRepository;
import kr.hhplus.be.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayHistoryService implements PayHistoryUseCase {

    private final UserRepository userRepository;
    private final PayHistoryRepository payHistoryRepository;

    // 성공이력은 기존 트랜잭션 롤백 시, 함께 롤백되어야 하므로 REQUIRED
    @Override
    @Transactional
    public void saveSuccessHistory(Seat seat, UUID userId, PaymentStatus status, PaymentReason reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("사용자가 존재하지 않습니다: userId = " + userId));
        PayHistory history = PayHistory.of(seat, user, status, reason);
        payHistoryRepository.save(history);
    }

    // 예외 상황에서도 결제 실패 이력을 반드시 남기기 위한 REQUIRES_NEW 트랜잭션 분리
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedHistory(Seat seat, UUID userId, PaymentStatus status, PaymentReason reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("사용자가 존재하지 않습니다: userId = " + userId));
        PayHistory history = PayHistory.of(seat, user, status, reason);
        payHistoryRepository.save(history);
    }
}
