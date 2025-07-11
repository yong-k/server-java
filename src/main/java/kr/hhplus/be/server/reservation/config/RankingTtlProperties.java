package kr.hhplus.be.server.reservation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reservation.ranking.ttl-days")
public class RankingTtlProperties {
    private int daily;
    private int weekly;
    private int monthly;
}
