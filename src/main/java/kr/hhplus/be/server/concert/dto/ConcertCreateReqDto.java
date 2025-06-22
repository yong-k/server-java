package kr.hhplus.be.server.concert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ConcertCreateReqDto {

    @NotBlank(message = "콘서트 이름은 필수입니다")
    @Size(min = 1, max = 100, message = "콘서트 이름은 1~100자 사이여야 합니다")
    private String name;
}
