package kr.hhplus.be.server.common.exception;

import kr.hhplus.be.server.point.exception.PointPolicyViolationException;
import kr.hhplus.be.server.reservation.exception.InvalidSeatStatusException;
import kr.hhplus.be.server.reservation.exception.InvalidSeatUserStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PointPolicyViolationException.class)
    public ResponseEntity<ErrorResponse> handlePointPolicyViolationException(PointPolicyViolationException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("POINT_POLICY_VIOLATION", e.getLocalizedMessage()));
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(DataNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("DATA_NOT_FOUND", e.getLocalizedMessage()));
    }

    @ExceptionHandler(InvalidSeatStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSeatStatusException(InvalidSeatStatusException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_SEAT_STATUS", e.getLocalizedMessage()));
    }

    @ExceptionHandler(InvalidSeatUserStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSeatUserStatusException(InvalidSeatUserStatusException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_SEAT_USER_STATUS", e.getLocalizedMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "에러가 발생했습니다."));
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", e.getLocalizedMessage()));
    }
}
