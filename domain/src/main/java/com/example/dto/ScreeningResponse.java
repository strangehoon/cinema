package com.example.dto;

import com.example.entity.Screening;
import lombok.Builder;
import lombok.Getter;
import java.time.format.DateTimeFormatter;

@Builder
@Getter
public class ScreeningResponse {

    private String startTime;
    private String endTime;

    public static ScreeningResponse of(Screening screening) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return ScreeningResponse.builder()
                .startTime(screening.getStartTime().format(formatter))
                .endTime(screening.getEndTime().format(formatter))
                .build();
    }
}
