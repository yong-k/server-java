package kr.hhplus.be.server.reservation.application.port.in;

import kr.hhplus.be.server.reservation.dto.*;

import java.util.UUID;

public interface ReservationUseCase {

    ReservationTokenRespDto issueToken(UUID userId);

    SeatReservationRespDto reserveSeat(UUID tokenId, SeatReservationReqDto dto);

    PaymentRespDto pay(UUID tokenId, PaymentReqDto dto);

}
