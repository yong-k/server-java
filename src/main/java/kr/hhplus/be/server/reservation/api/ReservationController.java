package kr.hhplus.be.server.reservation.api;

import kr.hhplus.be.server.reservation.application.port.in.ReservationUseCase;
import kr.hhplus.be.server.reservation.dto.*;
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

    private final ReservationUseCase reservationUseCase;

    @PostMapping("/reservation/token")
    public ResponseEntity<ReservationTokenRespDto> issueToken(@RequestBody ReservationTokenReqDto dto) {
        ReservationTokenRespDto token = reservationUseCase.issueToken(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @PostMapping("/reservation")
    public ResponseEntity<SeatReservationRespDto> reserveSeat(@RequestBody SeatReservationReqDto dto) {
        SeatReservationRespDto seat = reservationUseCase.reserveSeat(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    @PostMapping("/payment")
    public ResponseEntity<PaymentRespDto> pay(@RequestBody PaymentReqDto dto) {
        PaymentRespDto payment = reservationUseCase.pay(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
