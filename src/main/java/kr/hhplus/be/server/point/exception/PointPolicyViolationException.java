package kr.hhplus.be.server.point.exception;

public class PointPolicyViolationException extends RuntimeException {
    public PointPolicyViolationException(String message) {
        super(message);
    }

    public PointPolicyViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
