package kr.hhplus.be.server.reservation.application.port.in;

import kr.hhplus.be.server.reservation.dto.*;

import java.util.UUID;

public interface ReservationUseCase {

    ReservationTokenRespDto issueToken(ReservationTokenReqDto dto);

    SeatReservationRespDto reserveSeat(SeatReservationReqDto dto);

    PaymentRespDto pay(PaymentReqDto dto);

}
