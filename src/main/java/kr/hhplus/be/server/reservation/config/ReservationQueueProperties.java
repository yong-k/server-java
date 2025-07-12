package kr.hhplus.be.server.reservation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reservation.queue")
public class ReservationQueueProperties {
    private int allowedLimit;
    private int batchAllowedLimit;
    private long schedulerIntervalMs;
}
