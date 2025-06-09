package kr.hhplus.be.server.reservation.exception;

public class InvalidSeatStatusException extends RuntimeException {
    public InvalidSeatStatusException(String message) {
        super(message);
    }

    public InvalidSeatStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
