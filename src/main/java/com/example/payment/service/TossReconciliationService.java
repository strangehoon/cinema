package com.example.payment.service;

import com.example.reservation.service.BookingService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TossReconciliationService {
    private static final Logger log = LoggerFactory.getLogger(TossReconciliationService.class);
    private final BookingService bookings;
    private final TossGateway gateway;
    private final String mode;

    public TossReconciliationService(BookingService bookings, TossGateway gateway,
                                     @Value("${payment.mode:mock}") String mode) {
        this.bookings = bookings;
        this.gateway = gateway;
        this.mode = mode;
    }

    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    @SchedulerLock(name = "reconcile-toss-payments", lockAtMostFor = "PT5M")
    public void reconcile() {
        if (!mode.equals("toss")) return;
        for (BookingService.PendingToss pending : bookings.pendingToss()) {
            try {
                TossGateway.Result result = gateway.lookup(pending.orderId());
                if (result == null || !pending.orderId().equals(result.orderId()) ||
                        !pending.paymentKey().equals(result.paymentKey()) ||
                        pending.amount() != result.totalAmount()) continue;
                if ("DONE".equals(result.status())) {
                    bookings.finishToss(pending.orderId(), pending.userId(), pending.paymentKey());
                } else if ("CANCELED".equals(result.status()) || "ABORTED".equals(result.status())) {
                    bookings.failToss(pending.orderId(), pending.userId());
                }
            } catch (RuntimeException e) {
                log.warn("토스 결제 복구 재시도 대상 주문: {}", pending.orderId(), e);
            }
        }
        for (BookingService.PendingToss refund : bookings.pendingRefunds()) {
            try {
                TossGateway.Result result = gateway.lookup(refund.orderId());
                if (result == null || !refund.orderId().equals(result.orderId()) ||
                        !refund.paymentKey().equals(result.paymentKey()) ||
                        refund.amount() != result.totalAmount()) continue;
                if ("CANCELED".equals(result.status())) {
                    bookings.cancel(refund.orderId(), refund.userId());
                } else if ("DONE".equals(result.status())) {
                    gateway.cancel(refund.paymentKey(), refund.orderId());
                    bookings.cancel(refund.orderId(), refund.userId());
                }
            } catch (RuntimeException e) {
                log.warn("토스 환불 복구 재시도 대상 주문: {}", refund.orderId(), e);
            }
        }
    }
}
