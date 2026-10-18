package com.example.reservation.controller;

import com.example.reservation.dto.request.ReservationCreateServiceRequest;
import com.example.reservation.dto.response.ReservationCreateServiceResponse;
import com.example.reservation.service.BookingService;
import com.example.reservation.service.ReservationService;
import com.example.payment.service.TossCheckoutService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingApiController {
    private final ReservationService reservations;
    private final BookingService bookings;
    private final TossCheckoutService toss;

    @Value("${psp.toss.client-key:}")
    private String tossClientKey;

    public record PaymentConfig(String mode, String clientKey) {}
    public record TossConfirmRequest(@NotNull @Size(min = 1, max = 200) String paymentKey,
                                     @NotNull @Positive Long amount) {}

    @GetMapping("/payment-config")
    public PaymentConfig paymentConfig() {
        return new PaymentConfig(toss.mode(), toss.mode().equals("toss") ? tossClientKey : "");
    }

    public record CreateRequest(@NotNull @Positive Long screeningId,
                                @NotNull @Size(min = 1, max = 3) List<@NotNull @Positive Long> seatIds) {}

    @PostMapping
    public ReservationCreateServiceResponse create(@Valid @RequestBody CreateRequest body, Authentication authentication) {
        return reservations.createReserve(ReservationCreateServiceRequest.builder()
                .userId((Long) authentication.getPrincipal())
                .screeningId(body.screeningId()).seatIds(body.seatIds()).build());
    }

    @GetMapping
    public List<BookingService.OrderView> orders(Authentication authentication) {
        return bookings.orders((Long) authentication.getPrincipal());
    }

    @GetMapping("/{orderId}")
    public BookingService.OrderView order(@PathVariable String orderId, Authentication authentication) {
        return bookings.order(orderId, (Long) authentication.getPrincipal());
    }

    @PostMapping("/{orderId}/mock-payment")
    public BookingService.OrderView mockPayment(@PathVariable String orderId, Authentication authentication) {
        if (!toss.mode().equals("mock")) throw new IllegalStateException("모의 결제가 활성화되지 않았습니다.");
        return bookings.completeMock(orderId, (Long) authentication.getPrincipal());
    }

    @PostMapping("/{orderId}/mock-payment/fail")
    public void mockPaymentFailure(@PathVariable String orderId, Authentication authentication) {
        if (!toss.mode().equals("mock")) throw new IllegalStateException("모의 결제가 활성화되지 않았습니다.");
        bookings.failMock(orderId, (Long) authentication.getPrincipal());
    }

    @PostMapping("/{orderId}/toss-confirm")
    public BookingService.OrderView tossConfirm(@PathVariable String orderId,
                                                 @Valid @RequestBody TossConfirmRequest body,
                                                 Authentication authentication) {
        return toss.confirm(orderId, (Long) authentication.getPrincipal(), body.paymentKey(), body.amount());
    }

    @PostMapping("/{orderId}/cancel")
    public BookingService.OrderView cancel(@PathVariable String orderId, Authentication authentication) {
        return toss.cancel(orderId, (Long) authentication.getPrincipal());
    }
}
