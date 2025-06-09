package kr.hhplus.be.server.point.domain;

import kr.hhplus.be.server.point.exception.PointPolicyViolationException;

public class PointPolicy {
    public static final int MAX_CHARGE = 2_000_000;    // 1회 최대 충전 가능 포인트
    public static final int MAX_POINT = 2_000_000;     // 최대 보유 가능 포인트

    public static void validateCharge(int currentPoint, int chargeAmount) {
        if (chargeAmount <= 0)
            throw new PointPolicyViolationException("충전 금액은 0보다 커야합니다: 요청금액[" + chargeAmount + "]");

        // 1회 최대 충전 금액 체크
        if (chargeAmount > MAX_CHARGE)
            throw new PointPolicyViolationException("1회 최대 충전 금액은 " + String.format("%,d", MAX_CHARGE) + "원입니다: 요청금액[" + chargeAmount + "]");

        // 최대 보유 포인트 초과여부 체크
        int afterChargePoint = currentPoint + chargeAmount;
        if (afterChargePoint > MAX_POINT)
            throw new PointPolicyViolationException("포인트는 최대 " + String.format("%,d", MAX_POINT) + "원까지 보유할 수 있습니다: "
                    + "현재 포인트[" + currentPoint + "], 충전 포인트[" + chargeAmount + "]");
    }

    public static void validateUse(int currentPoint, int useAmount) {
        if (useAmount <= 0)
            throw new PointPolicyViolationException("사용 금액은 0보다 커야합니다: 요청금액[" + useAmount + "]");

        // 보유포인트 < 사용포인트 체크
        if (currentPoint < useAmount)
            throw new PointPolicyViolationException("포인트가 부족합니다: 현재 포인트[" + currentPoint + "], 사용 포인트[" + useAmount + "]");
    }
}
