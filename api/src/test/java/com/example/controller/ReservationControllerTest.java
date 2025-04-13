package com.example.controller;

import com.example.config.IntegrationControllerSupport;
import com.example.dto.request.ReservationRequest;
import com.example.dto.response.ReservationServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationControllerTest extends IntegrationControllerSupport {

    @Nested
    @DisplayName("좌석 예약")
    class reserveSeats {

        @Test
        @DisplayName("좌석 예약 요청이 들어오면 정상적으로 처리하고 응답한다")
        void reserveSeats_success() throws Exception {
            // given
            ReservationRequest request = ReservationRequest.builder()
                    .userId(1L)
                    .screeningId(1L)
                    .seatIds(List.of(1L, 2L))
                    .build();

            ReservationServiceResponse mockResponse = ReservationServiceResponse.builder()
                    .userId(1L)
                    .screeningId(1L)
                    .reservedSeatIds(List.of(1L, 2L))
                    .build();

            given(reservationService.reserveSeats(any())).willReturn(mockResponse);

            // when & then
            mockMvc.perform(
                            post("/reservations")
                                    .content(objectMapper.writeValueAsString(request))
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.userId").value(1L))
                    .andExpect(jsonPath("$.data.screeningId").value(1L))
                    .andExpect(jsonPath("$.data.reservedSeatIds[0]").value(1L))
                    .andExpect(jsonPath("$.data.reservedSeatIds[1]").value(2L))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest(name = "{index}: userId={0}, screeningId={1}, seatIds={2}")
        @MethodSource("invalidRequests")
        @DisplayName("유효하지 않은 좌석 예약 요청은 400 Bad Request를 반환한다")
        void reserveSeats_fail_invalidInputs(Long userId, Long screeningId, List<Long> seatIds) throws Exception {
            // given
            ReservationRequest request = ReservationRequest.builder()
                    .userId(userId)
                    .screeningId(screeningId)
                    .seatIds(seatIds)
                    .build();

            // when & then
            mockMvc.perform(
                            post("/reservations")
                                    .content(objectMapper.writeValueAsString(request))
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        static Stream<Arguments> invalidRequests() {
            return Stream.of(
                    // userId null
                    Arguments.of(null, 1L, Arrays.asList(1L)),
                    // userId < 0
                    Arguments.of(-1L, 1L, Arrays.asList(1L)),

                    // screeningId null
                    Arguments.of(1L, null, Arrays.asList(1L)),
                    // screeningId < 0
                    Arguments.of(1L, -1L, Arrays.asList(1L)),

                    // seatIds null
                    Arguments.of(1L, 1L, null),
                    // seatIds empty
                    Arguments.of(1L, 1L, Arrays.asList()),
                    // seatIds > 5
                    Arguments.of(1L, 1L, Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L)),
                    // seatIds with null
                    Arguments.of(1L, 1L, Arrays.asList(1L, null)),
                    // seatIds < 0
                    Arguments.of(1L, 1L, Arrays.asList(-1L))
            );
        }
    }
}