package kr.hhplus.be.server.reservation.application.port.out;

import java.util.List;
import java.util.UUID;

public interface ReservationQueueStore {

    /**
     * 대기열에 사용자 추가 (TTL이 만료되지 않은 ALLOWED, WAITING 사용자는 추가 X)
     *
     * @param tokenId 대기열토큰 ID
     * @param currentTimeMillis 현재 시간 (순번 정렬용)
     * @return 현재 대기열 내 순번
     */
    int addToQueueIfAbsent(UUID tokenId, long currentTimeMillis);

    /**
     * 대기열 내 tokenId의 현재 순번
     *
     * @param tokenId 대기열토큰 ID
     * @return 대기열 내 순번 (1부터 시작, 없을 경우 -1)
     */
    int getQueuePosition(UUID tokenId);

    /**
     * 현재 대기열에 존재하는 tokenId 중 상태가 WAITING인 토큰만 순번 순으로 조회
     *
     * @return 대기 중인(WAITING 상태) tokenId 목록 (순번 오름차순 정렬)
     */
    List<UUID> getWaitingQueueTokenIds(int limit);
}
