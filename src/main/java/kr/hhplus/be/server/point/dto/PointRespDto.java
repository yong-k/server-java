package kr.hhplus.be.server.point.dto;

import kr.hhplus.be.server.point.domain.PointHistory;
import kr.hhplus.be.server.point.domain.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class PointRespDto {
    private int id;
    private TransactionType type;
    private int amount;
    private int currentPoint;
    private LocalDateTime createdAt;

    @Builder
    public PointRespDto(int id, TransactionType type, int amount, int currentPoint, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.currentPoint = currentPoint;
        this.createdAt = createdAt;
    }

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
