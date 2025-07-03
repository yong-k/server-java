package kr.hhplus.be.server.reservation.infrastructure.external;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisDistributedLockManager {

    private final StringRedisTemplate redisTemplate;

    // 락 획득 (SETNX)
    public boolean lock(String key, String value, Duration expire) {
        // true(락 획득 성공), false(이미 존재해서 실패), null(예외적 상황 발생)
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, expire);
        return Boolean.TRUE.equals(success);
    }

    // 락 해제 (Lua 스크립트)
    public boolean unlock(String key, String value) {
        String lua = "if redis.call('get', KEYS[1]) == ARGV[1] "
                    + "then return redis.call('del', KEYS[1]) "
                    + "else return 0 end";

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(lua, Long.class),
                Collections.singletonList(key),
                value
        );

        return result != null && result > 0;
    }

    public String generateUniqueValue() {
        return UUID.randomUUID().toString();
    }
}
