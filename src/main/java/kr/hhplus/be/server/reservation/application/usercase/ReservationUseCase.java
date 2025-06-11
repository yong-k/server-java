package kr.hhplus.be.server.reservation.application.usercase;

import kr.hhplus.be.server.reservation.dto.PaymentReqDto;
import kr.hhplus.be.server.reservation.dto.PaymentRespDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationReqDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationRespDto;

public interface ReservationUseCase {

    SeatReservationRespDto reserveSeat(SeatReservationReqDto dto);

    PaymentRespDto pay(PaymentReqDto dto);

}
