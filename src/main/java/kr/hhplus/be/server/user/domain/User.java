package kr.hhplus.be.server.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.hhplus.be.server.point.exception.PointPolicyViolationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import static kr.hhplus.be.server.point.domain.PointPolicy.MAX_CHARGE;
import static kr.hhplus.be.server.point.domain.PointPolicy.MAX_POINT;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private UUID id;
    private String email;
    private String password;
    private String name;
    private String phone;
    private int point;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(UUID userId, int point) {
        this.id = userId;
        this.point = point;
    }

    public void validateAndCharge(int amount) {
        if (amount <= 0)
            throw new PointPolicyViolationException("충전 금액은 0보다 커야합니다: 요청금액[" + amount + "]");

        // 1회 최대 충전 금액 체크
        if (amount > MAX_CHARGE)
            throw new PointPolicyViolationException("1회 최대 충전 금액은 " + String.format("%,d", MAX_CHARGE) + "원입니다: 요청금액[" + amount + "]");

        // 최대 보유 포인트 초과여부 체크
        int afterChargePoint = point + amount;
        if (afterChargePoint > MAX_POINT)
            throw new PointPolicyViolationException("포인트는 최대 " + String.format("%,d", MAX_POINT) + "원까지 보유할 수 있습니다: 현재 포인트[" + point + "], 충전 포인트[" + amount + "]");

        this.point += amount;
    }

    public void validateAndUse(int amount) {
        if (amount <= 0)
            throw new PointPolicyViolationException("사용 금액은 0보다 커야합니다: 요청금액[" + amount + "]");

        // 보유포인트 < 사용포인트 체크
        if (point < amount)
            throw new PointPolicyViolationException("포인트가 부족합니다: 현재 포인트[" + point + "], 사용 포인트[" + amount + "]");

        this.point -= amount;
    }
}
