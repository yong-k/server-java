package kr.hhplus.be.server.point.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointReqDto {

    @Min(value = 1, message = "금액은 1 이상이어야 합니다")
    private int amount;
}
