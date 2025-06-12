package kr.hhplus.be.server.reservation.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private UUID userId;
    private String email;

    private int concertId;
    private String concertName;

    private int concertScheduleId;
    private LocalDateTime scheduleAt;

    private int seatId;
    private int seatNumber;
    private int seatPrice;

    private int amount;     // 실제결제금액

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String reason;

    @CreationTimestamp
    @Column(name="pay_at", updatable = false)
    private LocalDateTime payAt;

}
