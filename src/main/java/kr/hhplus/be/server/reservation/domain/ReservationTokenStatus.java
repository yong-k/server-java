package kr.hhplus.be.server.reservation.domain;

/**
 * 대기열토큰 상태 종류
 * - WAITING : 대기
 * - READY : 입장가능
 * - EXPIRED : 만료
 */
public enum ReservationTokenStatus {
    WAITING,
    READY,
    EXPIRED
}