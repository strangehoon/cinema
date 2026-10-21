package com.example.payment.service;

import com.example.reservation.service.BookingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class TossCheckoutService {
    private final BookingService bookings;
    private final TossGateway gateway;
    private final String mode;
    private final Clock clock;

    public TossCheckoutService(BookingService bookings, TossGateway gateway,
                               @Value("${payment.mode:mock}") String mode, Clock clock) {
        this.bookings = bookings;
        this.gateway = gateway;
        this.mode = mode;
        this.clock = clock;
    }

    public BookingService.OrderView confirm(String orderId, Long userId, String paymentKey, long amount) {
        requireToss();
        BookingService.OrderView order = bookings.order(orderId, userId);
        if (order.status().equals("DONE")) return order;
        if ((!order.status().equals("READY") && !order.status().equals("IN_PROGRESS")) ||
                (order.status().equals("READY") && (order.expiresAt() == null ||
                        !order.expiresAt().isAfter(LocalDateTime.now(clock)))) || order.amount() != amount) {
            throw new IllegalArgumentException("결제 가능한 주문과 금액을 확인해 주세요.");
        }
        bookings.startToss(orderId, userId, paymentKey);
        TossGateway.Result result;
        try {
            result = gateway.confirm(paymentKey, orderId, order.amount());
        } catch (HttpClientErrorException rejected) {
            bookings.resetToss(orderId, userId);
            throw rejected;
        }
        if (result == null || !"DONE".equals(result.status()) ||
                !orderId.equals(result.orderId()) || !paymentKey.equals(result.paymentKey()) ||
                result.totalAmount() != order.amount()) {
            throw new IllegalStateException("결제 승인 결과가 주문과 일치하지 않습니다.");
        }
        try {
            return bookings.finishToss(orderId, userId, paymentKey);
        } catch (RuntimeException failure) {
            try {
                gateway.cancel(paymentKey, orderId);
                bookings.failToss(orderId, userId);
            } catch (RuntimeException refundFailure) {
                failure.addSuppressed(refundFailure);
            }
            throw failure;
        }
    }

    public BookingService.OrderView cancel(String orderId, Long userId) {
        BookingService.OrderView order = bookings.order(orderId, userId);
        if (!order.status().equals("DONE") && !order.status().equals("REFUNDING"))
            return bookings.cancel(orderId, userId);
        String paymentKey = bookings.paymentKey(orderId, userId);
        if (paymentKey == null) return bookings.cancel(orderId, userId);
        requireToss();
        if (order.status().equals("DONE") && order.startsAt() != null &&
                !order.startsAt().isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("상영이 시작된 예매는 취소할 수 없습니다.");
        }
        bookings.beginTossRefund(orderId, userId);
        gateway.cancel(paymentKey, orderId);
        return bookings.cancel(orderId, userId);
    }

    public String mode() { return mode; }

    private void requireToss() {
        if (!mode.equals("toss")) throw new IllegalStateException("토스 결제가 활성화되지 않았습니다.");
    }
}
