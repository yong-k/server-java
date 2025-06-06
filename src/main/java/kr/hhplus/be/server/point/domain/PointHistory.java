package kr.hhplus.be.server.point.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private UUID userId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private int amount;

    private int currentPoint;

    @Column(name="created_at", insertable = false)
    private LocalDateTime createdAt;
}
