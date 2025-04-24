package com.example.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
public class ReservationRequest {

    @NotNull(message = "회원 ID는 필수입니다.")
    @Positive(message = "회원 ID는 양수여야 합니다.")
    private Long userId;

    @NotNull(message = "상영 시간표 ID는 필수입니다.")
    @Positive(message = "상영 시간표 ID는 양수여야 합니다.")
    private Long screeningId;

    @NotNull(message = "좌석 목록은 필수입니다.")
    @Size(min = 1, max = 5, message = "좌석은 최소 1개, 최대 5개까지 선택할 수 있습니다.")
    private List<@NotNull(message = "좌석 ID는 null일 수 없습니다.")
    @Positive(message = "좌석 ID는 양수여야 합니다.")
            Long> seatIds;

    @Builder
    private ReservationRequest(Long userId, Long screeningId, List<Long> seatIds){
        this.userId = userId;
        this.screeningId = screeningId;
        this.seatIds = seatIds;
    }

    public ReservationServiceRequest toServiceRequest() {
        return ReservationServiceRequest.builder()
                .userId(userId)
                .screeningId(screeningId)
                .seatIds(seatIds)
                .build();
    }
    public ReservationRequest of(Long userId, Long screeningId, List<Long> seatIds) {
        return ReservationRequest.builder()
                .userId(userId)
                .screeningId(screeningId)
                .seatIds(seatIds)
                .build();
    }
}