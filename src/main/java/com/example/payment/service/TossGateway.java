package com.example.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class TossGateway {
    private final RestClient client;

    public record Result(String paymentKey, String orderId, long totalAmount, String status) {}

    public TossGateway(@Value("${psp.toss.secret-key:dummy}") String secretKey,
                       @Value("${psp.toss.url:https://api.tosspayments.com}") String url) {
        var requests = new SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(2000);
        requests.setReadTimeout(5000);
        String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        client = RestClient.builder().baseUrl(url).requestFactory(requests)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth).build();
    }

    public Result confirm(String paymentKey, String orderId, long amount) {
        try {
            return client.post().uri("/v1/payments/confirm")
                    .header("Idempotency-Key", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                    .retrieve().body(Result.class);
        } catch (RestClientException failure) {
            Result recovered = lookup(orderId);
            if (recovered != null && "DONE".equals(recovered.status())) return recovered;
            throw failure;
        }
    }

    public Result lookup(String orderId) {
        try {
            return client.get().uri("/v1/payments/orders/{orderId}", orderId)
                    .retrieve().body(Result.class);
        } catch (HttpClientErrorException.NotFound missing) {
            return null;
        }
    }

    public void cancel(String paymentKey, String orderId) {
        client.post().uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .header("Idempotency-Key", "cancel-" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("cancelReason", "고객 요청"))
                .retrieve().toBodilessEntity();
    }
}
