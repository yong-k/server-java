package kr.hhplus.be.server.concert.domain;

/**
 * 좌석 상태 종류
 * - AVAILABLE : 예매 가능
 * - TEMP_RESERVED : 임시 배정
 * - RESERVED : 예매된 좌석
 * - EXPIRED : 만료
 * - HOLD : 보류
 */
public enum SeatStatus {
    AVAILABLE,
    TEMP_RESERVED,
    RESERVED,
    EXPIRED,
    HOLD
}
