package kr.hhplus.be.server.reservation.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.reservation.exception.InvalidReservationTokenException;
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
public class ReservationToken {
    @Id
    private UUID id;

    private UUID userId;
    private int concertId;

    @Column(name="`order`")
    private int order;

    @Enumerated(EnumType.STRING)
    private ReservationTokenStatus status;

    @CreationTimestamp
    @Column(name="issued_at", updatable = false)
    private LocalDateTime issuedAt;

    private LocalDateTime expiredAt;

    public void expire() {
        this.status = ReservationTokenStatus.EXPIRED;
        this.expiredAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiredAt != null && expiredAt.isBefore(LocalDateTime.now());
    }
}
