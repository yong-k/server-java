package kr.hhplus.be.server.reservation.infrastructure.external;

import kr.hhplus.be.server.reservation.application.port.out.ReservationQueueStore;
import kr.hhplus.be.server.reservation.application.port.out.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.ReservationToken;
import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisReservationQueueStore implements ReservationQueueStore {

    private final StringRedisTemplate redisTemplate;
    private final ReservationTokenRepository reservationTokenRepository;

    private static final String QUEUE_KEY = "queue:reservation";


    /**
     * Redis의 ZSET (Sorted Set) 사용
     * - 값(value)과 점수(score)를 함께 저장해서 자동으로 점수 기준 정렬 리스트 유지해주는 거 활용
     * - value : tokenId
     * - score : timestamp
     *
     * @param tokenId 대기열토큰 ID
     * @param currentTimeMillis 현재 시간 (순번 정렬용)
     * @return 대기열에 추가된 후의 순번 (1부터 시작), 이미 존재하는 경우 기존 순번 반환, 예외상황 시 -1
     */
    @Override
    public int addToQueueIfAbsent(UUID tokenId, long currentTimeMillis) {
        String tokenKey = tokenId.toString();   // Redis 문자열 키만 다룸

        Long rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, tokenKey);
        if (rank == null) {
            redisTemplate.opsForZSet().add(QUEUE_KEY, tokenKey, currentTimeMillis);
            rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, tokenKey);
        }
        return rank != null ? rank.intValue() + 1 : -1;     // Redis는 0부터 시작해서 + 1 해줌
    }

    /**
     * 대기열에서 현재 사용자의 순번 조회
     *
     * @param tokenId 대기열토큰 ID
     * @return 대기열 내 순번 (1부터 시작), 존재하지 않으면 -1
     */
    @Override
    public int getQueuePosition(UUID tokenId) {
        String tokenKey = tokenId.toString();   // Redis 문자열 키만 다룸
        Long rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, tokenKey);
        return rank != null ? rank.intValue() + 1 : -1;     // Redis는 0부터 시작해서 + 1 해줌
    }

    /**
     * 현재 대기열에 존재하는 tokenId 중 상태가 WAITING인 토큰만 순번 순으로 조회
     *
     * @return 대기 중인(WAITING 상태) tokenId 목록 (순번 오름차순 정렬)
     */
    @Override
    public List<UUID> getWaitingQueueTokenIds(int limit) {
        // Redis ZSET에서 가장 앞쪽 limit * 2개만 가져오기 (waiting말고 다른 상태값인 토큰 있을 수도 있으니)
        int redisFetchLimit = limit * 2;
        Set<String> tokenStrs = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, redisFetchLimit - 1);
        if (tokenStrs == null || tokenStrs.isEmpty()) {
            return Collections.emptyList();
        }

        // Redis 키는 문자열이므로, UUID로 변환
        List<UUID> tokenIds = tokenStrs.stream()
                .map(UUID::fromString)
                .toList();

        // 상태 필터링: WAITING
        return reservationTokenRepository.findAllById(tokenIds).stream()
                .filter(token -> token.getStatus() == ReservationTokenStatus.WAITING)
                .map(ReservationToken::getId)
                .toList();
    }
}
