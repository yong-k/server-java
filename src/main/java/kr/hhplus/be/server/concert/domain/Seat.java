package kr.hhplus.be.server.concert.domain;

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
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_schedule_id")
    private ConcertSchedule concertSchedule;

    private int number;
    private int price;
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private LocalDateTime releasedAt;
    private LocalDateTime reservedAt;

    public void reserve(UUID userId) {
        this.userId = userId;
        this.status = SeatStatus.TEMP_RESERVED;
        this.releasedAt = LocalDateTime.now().plusMinutes(5);   // 임시배정 5분 후 → 만료 처리
    }

    public void pay() {
        this.status = SeatStatus.RESERVED;
        this.reservedAt = LocalDateTime.now();
        this.releasedAt = null;
    }

    public void expire() {
        this.status = SeatStatus.EXPIRED;
        this.releasedAt = LocalDateTime.now().plusMinutes(1);   // 만료 1분 후 → 보류 처리
    }

    public void hold() {
        this.status = SeatStatus.HOLD;
        this.releasedAt = LocalDateTime.now().plusMinutes(3);   // 보류 3분 후 → 예약가능 처리
    }

    public void release() {
        this.userId = null;
        this.status = SeatStatus.AVAILABLE;
        this.releasedAt = null;
    }
}
