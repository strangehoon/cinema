package com.example.reservation;

import com.example.reservation.controller.BookingApiController;
import com.example.reservation.dto.response.ReservationCreateServiceResponse;
import com.example.reservation.service.BookingService;
import com.example.reservation.service.ReservationService;
import com.example.payment.service.TossCheckoutService;
import com.example.reservation.exception.ReservationException;
import com.example.reservation.exception.ReservationErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingApiControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ReservationService reservations;
    @MockitoBean BookingService bookings;
    @MockitoBean TossCheckoutService toss;

    @Nested
    @DisplayName("좌석 예매 API")
    class Create {
        @Test
        @DisplayName("사용자 ID를 요청 본문이 아닌 로그인 세션에서 가져온다")
        void success() throws Exception {
            // given
            given(reservations.createReserve(argThat(r -> r.getUserId().equals(7L))))
                    .willReturn(ReservationCreateServiceResponse.builder()
                            .orderId("order-7").orderName("영화 1매").totalAmount(12000L).build());

            // when & then
            mvc.perform(post("/api/bookings").principal(new UsernamePasswordAuthenticationToken(7L, null, List.of()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":99,\"screeningId\":1,\"seatIds\":[1]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value("order-7"));
        }

        @Test
        @DisplayName("이미 점유된 좌석은 409를 반환한다")
        void fail_reserved_seat() throws Exception {
            // given
            given(reservations.createReserve(argThat(r -> r.getUserId().equals(7L))))
                    .willThrow(new ReservationException(ReservationErrorCode.ALREADY_RESERVED_SEAT));

            // when & then
            mvc.perform(post("/api/bookings").principal(new UsernamePasswordAuthenticationToken(7L, null, List.of()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"screeningId\":1,\"seatIds\":[1]}"))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("결제 API")
    class Payment {
        @Test
        @DisplayName("결제사 통신 오류는 502와 안내 메시지를 반환한다")
        void fail_gateway() throws Exception {
            // given
            given(toss.confirm("order-7", 7L, "key-7", 12000L))
                    .willThrow(new RestClientException("provider unavailable"));

            // when & then
            mvc.perform(post("/api/bookings/order-7/toss-confirm")
                            .principal(new UsernamePasswordAuthenticationToken(7L, null, List.of()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"paymentKey\":\"key-7\",\"amount\":12000}"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}
