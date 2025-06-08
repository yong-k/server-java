package kr.hhplus.be.server.concert.dto;

import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatRespDto {
    private int id;
    private int number;
    private int price;
    private SeatStatus status;

    public static SeatRespDto from(Seat entity) {
        return SeatRespDto.builder()
                .id(entity.getId())
                .number(entity.getNumber())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .build();
    }
}
