package kr.hhplus.be.server.point.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserPoint {
    private UUID userId;
    private int point;

    public int charge(int amount) {
        PointPolicy.validateCharge(point, amount);
        point += amount;
        return point;
    }

    public int use(int amount) {
        PointPolicy.validateUse(point, amount);
        point -= amount;
        return point;
    }
}
