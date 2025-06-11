package kr.hhplus.be.server.reservation.infrastructure.persistence.jpa;

import kr.hhplus.be.server.reservation.domain.PayHistory;
import kr.hhplus.be.server.reservation.domain.PayHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PayHistoryRepositoryImpl implements PayHistoryRepository {

    private final JpaPayHistoryRepository jpaRepository;

    @Override
    public PayHistory save(PayHistory payHistory) {
        return jpaRepository.save(payHistory);
    }
}
