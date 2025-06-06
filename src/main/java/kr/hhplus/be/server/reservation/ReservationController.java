package kr.hhplus.be.server.reservation;

import kr.hhplus.be.server.reservation.dto.PaymentReqDto;
import kr.hhplus.be.server.reservation.dto.PaymentRespDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationReqDto;
import kr.hhplus.be.server.reservation.dto.SeatReservationRespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /* 좌석예약
    available 상태의 좌석을 선택하면
    해당 좌석은 temp_reserved 상태로 변경
    (5분 후에도 결제가 이뤄지지 않으면 expired로 변경[로그 기록]
    1분 후에 hold로 변경
    3분 후에 available로 변경)*/
    @PostMapping("/reservation")
    public ResponseEntity<SeatReservationRespDto> reserveSeat(@RequestBody SeatReservationReqDto dto) {
        SeatReservationRespDto seat = reservationService.reserveSeat(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    /* 결제
    해당 좌석의 status가 temp_reserved이고, user_id가 해당 사용자인지 확인
    if 확인결과 일치한다면,
        사용자의 잔액을 확인하고 차감해
        해당 좌석의 상태를 reserved로 변경*/
    @PostMapping("/payment")
    public ResponseEntity<PaymentRespDto> pay(@RequestBody PaymentReqDto dto) {
        PaymentRespDto payment = reservationService.pay(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
