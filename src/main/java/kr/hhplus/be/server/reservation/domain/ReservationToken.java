package kr.hhplus.be.server.reservation.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.reservation.exception.InvalidReservationTokenException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationToken {
    @Id
    private UUID id;

    private UUID userId;

    @Column(name="`order`")
    private int order;

    @Enumerated(EnumType.STRING)
    private ReservationTokenStatus status;

    @CreationTimestamp
    @Column(name="issued_at", updatable = false)
    private LocalDateTime issuedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime expiredAt;

    public void timeout() {
        this.status = ReservationTokenStatus.TIMEOUT;
        this.expiredAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = ReservationTokenStatus.COMPLETED;
        this.expiredAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiredAt != null && expiredAt.isBefore(LocalDateTime.now());
    }
}
