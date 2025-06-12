package kr.hhplus.be.server.reservation.application.port.out;

import kr.hhplus.be.server.reservation.domain.PayHistory;

public interface PayHistoryRepository {
    PayHistory save(PayHistory payHistory);
}
