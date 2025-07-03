package kr.hhplus.be.server.reservation.domain;

/**
 * 대기열토큰 상태 종류
 * - WAITING    : 대기
 * - ALLOWED    : 입장가능
 * - TIMEOUT    : 만료(시간초과)
 * - COMPLETED  : 만료(결제완료)
 */
public enum ReservationTokenStatus {
    WAITING,
    ALLOWED,
    TIMEOUT,
    COMPLETED,
}