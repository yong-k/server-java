package kr.hhplus.be.server.reservation.exception;

public class InvalidReservationTokenException extends RuntimeException {
    public InvalidReservationTokenException(String message) {
        super(message);
    }

    public InvalidReservationTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
