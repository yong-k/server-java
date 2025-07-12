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

    @Enumerated(EnumType.STRING)
    private ReservationTokenStatus status;

    @CreationTimestamp
    @Column(name="issued_at", updatable = false)
    private LocalDateTime issuedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime expiredAt;

    // ALLOWED된 후, 일정 시간 내에 결제되지 않으면 TIMEOUT 처리를 위해 expiredAt 설정 (스케줄러)
    //--나중에 대기열 구현하면서, 토큰 allowed 처리할 때 사용 예정
    public void allow(long allowedToTimeoutMinutes) {
        this.status = ReservationTokenStatus.ALLOWED;
        this.expiredAt = LocalDateTime.now().plusMinutes(allowedToTimeoutMinutes);
    }

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
