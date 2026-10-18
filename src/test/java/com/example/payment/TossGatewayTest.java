package com.example.payment;

import com.example.payment.service.TossGateway;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TossGatewayTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) server.stop(0);
    }

    @Nested
    @DisplayName("토스 HTTP 계약")
    class Gateway {
        @Test
        @DisplayName("승인 요청에 비밀 키와 멱등 키, 주문 금액을 전송한다")
        void success_confirm() throws Exception {
            // given
            AtomicReference<String> authorization = new AtomicReference<>();
            AtomicReference<String> idempotency = new AtomicReference<>();
            AtomicReference<String> requestBody = new AtomicReference<>();
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/v1/payments/confirm", exchange -> {
                authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                idempotency.set(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
                requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                byte[] response = "{\"paymentKey\":\"key-1\",\"orderId\":\"order-1\",\"totalAmount\":10000,\"status\":\"DONE\"}"
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            });
            server.start();
            TossGateway gateway = new TossGateway("test-secret", "http://127.0.0.1:" + server.getAddress().getPort());

            // when
            TossGateway.Result result = gateway.confirm("key-1", "order-1", 10000);

            // then
            assertThat(result.status()).isEqualTo("DONE");
            assertThat(authorization.get()).isEqualTo("Basic dGVzdC1zZWNyZXQ6");
            assertThat(idempotency.get()).isEqualTo("order-1");
            assertThat(requestBody.get()).contains("\"amount\":10000", "\"orderId\":\"order-1\"");
        }

        @Test
        @DisplayName("주문 없음과 결제사 장애를 구분한다")
        void success_lookup_distinguishes_failure() throws Exception {
            // given
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/v1/payments/orders/missing", exchange -> {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
            });
            server.createContext("/v1/payments/orders/error", exchange -> {
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
            });
            server.start();
            TossGateway gateway = new TossGateway("test-secret", "http://127.0.0.1:" + server.getAddress().getPort());

            // when & then
            assertThat(gateway.lookup("missing")).isNull();
            assertThatThrownBy(() -> gateway.lookup("error"))
                    .isInstanceOf(org.springframework.web.client.RestClientException.class);
        }

        @Test
        @DisplayName("전액 취소 요청에 주문별 고정 멱등 키를 사용한다")
        void success_cancel_idempotency() throws Exception {
            // given
            AtomicReference<String> idempotency = new AtomicReference<>();
            AtomicReference<String> requestBody = new AtomicReference<>();
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/v1/payments/key-1/cancel", exchange -> {
                idempotency.set(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
                requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
            });
            server.start();
            TossGateway gateway = new TossGateway("test-secret", "http://127.0.0.1:" + server.getAddress().getPort());

            // when
            gateway.cancel("key-1", "order-1");
            gateway.cancel("key-1", "order-1");

            // then
            assertThat(idempotency.get()).isEqualTo("cancel-order-1");
            assertThat(requestBody.get()).contains("cancelReason").doesNotContain("cancelAmount");
        }
    }
}
