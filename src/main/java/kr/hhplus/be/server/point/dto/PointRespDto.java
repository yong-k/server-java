package kr.hhplus.be.server.point.dto;

import kr.hhplus.be.server.point.domain.PointHistory;
import kr.hhplus.be.server.point.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointRespDto {
    private int id;
    private TransactionType type;
    private int amount;
    private int currentPoint;
    private LocalDateTime createdAt;

    public static PointRespDto from(PointHistory entity) {
        return PointRespDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .currentPoint(entity.getCurrentPoint())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
