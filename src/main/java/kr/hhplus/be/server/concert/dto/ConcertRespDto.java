package kr.hhplus.be.server.concert.dto;

import kr.hhplus.be.server.concert.domain.Concert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertRespDto {
    private int id;
    private String name;

    public static ConcertRespDto from(Concert entity) {
        return ConcertRespDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
