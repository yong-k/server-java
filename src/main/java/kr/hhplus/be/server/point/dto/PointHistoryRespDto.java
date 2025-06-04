package kr.hhplus.be.server.point.dto;

import kr.hhplus.be.server.point.domain.PointHistory;
import kr.hhplus.be.server.point.domain.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class PointHistoryRespDto {
    private int id;
    private TransactionType type;
    private int amount;
    private int currentPoint;
    private LocalDateTime createdAt;

    @Builder
    public PointHistoryRespDto(int id, TransactionType type, int amount, int currentPoint, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.currentPoint = currentPoint;
        this.createdAt = createdAt;
    }

    public static PointHistoryRespDto from(PointHistory entity) {
        return PointHistoryRespDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .currentPoint(entity.getCurrentPoint())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
