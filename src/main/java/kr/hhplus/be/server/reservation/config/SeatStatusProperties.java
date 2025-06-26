package kr.hhplus.be.server.reservation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reservation.seat-status")
public class SeatStatusProperties {
    private long schedulerIntervalMs;
    private long tempReservedToExpiredMinutes;
    private long expiredToHoldMinutes;
    private long holdToAvailableMinutes;
}
