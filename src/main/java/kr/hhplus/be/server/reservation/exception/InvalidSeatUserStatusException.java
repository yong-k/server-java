package kr.hhplus.be.server.reservation.exception;

public class InvalidSeatUserStatusException extends RuntimeException {
    public InvalidSeatUserStatusException(String message) {
        super(message);
    }

    public InvalidSeatUserStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
