package kr.hhplus.be.server.reservation.infrastructure.external;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SeatLockManager {
    private final Map<Integer, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public void lockSeat(int seatId) {
        lockMap.computeIfAbsent(seatId, id -> new ReentrantLock()).lock();
    }

    public void unlockSeat(int seatId) {
        ReentrantLock lock = lockMap.get(seatId);
        if (lock != null && lock.isHeldByCurrentThread())
            lock.unlock();
    }
}
