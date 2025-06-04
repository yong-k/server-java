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
@RequestMapping("/api")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/v1/users/{userId}/points/histories")
    public ResponseEntity<List<PointHistoryRespDto>> history(@PathVariable("userId") UUID userId) {
        List<PointHistoryRespDto> histories = pointService.findByUserId(userId);
        return ResponseEntity.ok(histories);
    }

    @PostMapping("/v1/users/{userId}/points/charge")
    public ResponseEntity<PointRespDto> charge(@PathVariable("userId") UUID userId, @RequestBody PointReqDto pointReqDto) {
        PointRespDto point = pointService.charge(userId, pointReqDto.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(point);
    }

    @PostMapping("/v1/users/{userId}/points/use")
    public ResponseEntity<PointRespDto> use(@PathVariable("userId") UUID userId, @RequestBody PointReqDto pointReqDto) {
        PointRespDto point = pointService.use(userId, pointReqDto.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(point);
    }

}
