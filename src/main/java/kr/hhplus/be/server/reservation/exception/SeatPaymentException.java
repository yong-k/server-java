package kr.hhplus.be.server.reservation.exception;

public class SeatPaymentException extends RuntimeException {
    public SeatPaymentException(String message) {
        super(message);
    }

    public SeatPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
