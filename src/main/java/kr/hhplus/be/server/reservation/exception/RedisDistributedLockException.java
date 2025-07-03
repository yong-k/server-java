package kr.hhplus.be.server.reservation.exception;

public class RedisDistributedLockException extends RuntimeException {
    public RedisDistributedLockException(String message) {
        super(message);
    }

    public RedisDistributedLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
