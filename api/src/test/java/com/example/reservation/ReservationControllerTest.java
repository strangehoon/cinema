package com.example.reservation;

import com.example.config.IntegrationControllerSupport;
import com.example.reservation.dto.response.ReservationCreateServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.List;
import java.util.stream.Stream;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationControllerTest extends IntegrationControllerSupport {

    @Nested
    @DisplayName("좌석 예약")
    class ReserveSeats {

        @Test
        @DisplayName("좌석 예약 요청이 들어오면 정상적으로 처리하고 응답한다")
        void success() throws Exception {
            // given
            ReservationCreateServiceResponse serviceResponse = ReservationCreateServiceResponse.builder()
                    .orderId("ORDER1234")
                    .orderName("영화 관람권")
                    .totalAmount(24000L)
                    .build();

            given(reservationService.createReserve(any())).willReturn(serviceResponse);

            // when & then
            mockMvc.perform(post("/reservations")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("userId", "1")
                            .param("screeningId", "1")
                            .param("seatIds", "1", "2"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("checkout"))
                    .andExpect(model().attributeExists("reservation"))
                    .andExpect(model().attribute("reservation", hasProperty("orderName", is("영화 관람권"))))
                    .andExpect(model().attribute("reservation", hasProperty("totalAmount", is(24000L))));
        }

        @ParameterizedTest(name = "{index}: userId={0}, screeningId={1}, seatIds={2}")
        @MethodSource("invalidRequests")
        @DisplayName("유효하지 않은 좌석 예약 요청은 400 Bad Request를 반환한다")
        void fail_invalid_inputs(String userId, String screeningId, List<String> seatIds) throws Exception {
            // when
            MockHttpServletRequestBuilder requestBuilder = post("/reservations")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED);

            if (userId != null) {
                requestBuilder.param("userId", userId);
            }
            if (screeningId != null) {
                requestBuilder.param("screeningId", screeningId);
            }
            if (seatIds != null && !seatIds.isEmpty()) {
                requestBuilder.param("seatIds", seatIds.toArray(new String[0]));
            }

            // then
            mockMvc.perform(requestBuilder)
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidRequests() {
            return Stream.of(
                    // userId null
                    Arguments.of(null, "1", List.of("1")),
                    // userId < 0
                    Arguments.of("-1", "1", List.of("1")),

                    // screeningId null
                    Arguments.of("1", null, List.of("1")),
                    // screeningId < 0
                    Arguments.of("1", "-1", List.of("1")),

                    // seatIds null
                    Arguments.of("1", "1", null),
                    // seatIds empty
                    Arguments.of("1", "1", List.of()),
                    // seatIds > 5
                    Arguments.of("1", "1", List.of("1", "2", "3", "4", "5", "6")),
                    // seatIds contains 빈 문자열 (null 대체)
                    Arguments.of("1", "1", List.of("1", "")),
                    // seatIds < 0
                    Arguments.of("1", "1", List.of("-1"))
            );
        }
    }
}