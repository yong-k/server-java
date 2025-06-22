package kr.hhplus.be.server.reservation.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertSchedule;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.user.domain.User;
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

    public static PayHistory of(Seat seat, User user, PaymentStatus status, PaymentReason reason) {
        ConcertSchedule schedule = seat.getConcertSchedule();
        Concert concert = schedule.getConcert();

        return PayHistory.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .concertId(concert.getId())
                .concertName(concert.getName())
                .concertScheduleId(schedule.getId())
                .scheduleAt(schedule.getScheduleAt())
                .seatId(seat.getId())
                .seatNumber(seat.getNumber())
                .seatPrice(seat.getPrice())
                .amount(seat.getPrice())   //--수정필요) 나중에 할인같은거 생기면, 실제 결제금액을 넣어야한다.
                .status(status)
                .reason(reason != null ? reason.getMessage() : null)
                .build();
    }

}
