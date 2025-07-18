package kr.hhplus.be.server.reservation.api;

import kr.hhplus.be.server.reservation.application.port.in.ReservationQueueUseCase;
import kr.hhplus.be.server.reservation.application.service.facade.ReservationEntryFacade;
import kr.hhplus.be.server.reservation.dto.QueueEnterRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReservationQueueController {

    private final ReservationEntryFacade reservationEntryFacade;
    private final ReservationQueueUseCase reservationQueueUseCase;

    // 대기열 진입 요청 (토큰 발급 + 대기열 큐 진입 통합 처리)
    //--- X-USER-ID: 대기열토큰 발급 시, userId 필요. 아직 jwt 적용 전이라 header로 userId 값 보내서 테스트 진행중 (변경 예정)
    @PostMapping("/reservation/queue")
    public ResponseEntity<QueueEnterRespDto> enterQueue(@RequestHeader("X-USER-ID") UUID userId) {
        QueueEnterRespDto respDto = reservationEntryFacade.issueTokenAndEnterQueue(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(respDto);
    }
}
