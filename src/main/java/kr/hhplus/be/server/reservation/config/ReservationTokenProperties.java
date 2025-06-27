package kr.hhplus.be.server.reservation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reservation.token")
public class ReservationTokenProperties {
    private long schedulerIntervalMs;
    private long allowedToTimeoutMinutes;
}
