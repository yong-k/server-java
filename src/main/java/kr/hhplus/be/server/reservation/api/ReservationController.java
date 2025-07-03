package kr.hhplus.be.server.reservation.api;

import kr.hhplus.be.server.reservation.application.port.in.ReservationUseCase;
import kr.hhplus.be.server.reservation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationUseCase reservationUseCase;

    // 대기열 토큰 발급
    //--- X-USER-ID: 대기열토큰 발급 시, userId 필요. 아직 jwt 적용 전이라 header로 userId 값 보내서 테스트 진행중 (변경 예정)
    @PostMapping("/reservation/token")
    public ResponseEntity<ReservationTokenRespDto> issueToken(@RequestHeader("X-USER-ID") UUID userId) {
        ReservationTokenRespDto token = reservationUseCase.issueToken(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    // 좌석 예약
    @PostMapping("/reservation")
    public ResponseEntity<SeatReservationRespDto> reserveSeat(@RequestHeader("X-TOKEN-ID") UUID tokenId, @RequestBody SeatReservationReqDto dto) {
        SeatReservationRespDto seat = reservationUseCase.reserveSeat(tokenId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    // 결제
    @PostMapping("/payment")
    public ResponseEntity<PaymentRespDto> pay(@RequestHeader("X-TOKEN-ID") UUID tokenId, @RequestBody PaymentReqDto dto) {
        PaymentRespDto payment = reservationUseCase.pay(tokenId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
