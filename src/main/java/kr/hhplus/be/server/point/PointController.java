package kr.hhplus.be.server.point;

import kr.hhplus.be.server.point.dto.PointHistoryRespDto;
import kr.hhplus.be.server.point.dto.PointReqDto;
import kr.hhplus.be.server.point.dto.PointRespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/users/{userId}/points/histories")
    public ResponseEntity<List<PointHistoryRespDto>> getPointHistories(@PathVariable("userId") UUID userId) {
        List<PointHistoryRespDto> histories = pointService.getPointHistories(userId);
        return ResponseEntity.ok(histories);
    }

    @PostMapping("/users/{userId}/points/charge")
    public ResponseEntity<PointRespDto> chargePoint(@PathVariable("userId") UUID userId, @RequestBody PointReqDto pointReqDto) {
        PointRespDto point = pointService.chargePoint(userId, pointReqDto.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(point);
    }

    @PostMapping("/users/{userId}/points/use")
    public ResponseEntity<PointRespDto> usePoint(@PathVariable("userId") UUID userId, @RequestBody PointReqDto pointReqDto) {
        PointRespDto point = pointService.usePoint(userId, pointReqDto.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(point);
    }

}
