package com.example.payment.controller;

import com.example.exception.PaymentConfirmException;
import com.example.exception.PaymentConfirmErrorCode;
import com.example.interceptor.PaymentExceptionInterceptor;
import com.example.payment.PaymentProperties;
import com.example.payment.dto.request.TossPaymentConfirmRequest;
import com.example.payment.dto.response.TossPaymentConfirmFailResponse;
import com.example.payment.dto.response.TossPaymentConfirmResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private static final int CONNECT_TIMEOUT_SECONDS = 1;
    private static final int READ_TIMEOUT_SECONDS = 30;
    private static final String BASIC_DELIMITER = ":";
    private static final String AUTH_HEADER_PREFIX = "Basic ";

    private final ObjectMapper objectMapper;
    private RestClient restClient;
    private final PaymentProperties paymentProperties;

    @PostConstruct
    private void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_SECONDS * 1000); // milliseconds
        factory.setReadTimeout(READ_TIMEOUT_SECONDS * 1000);       // milliseconds

        this.restClient = RestClient.builder()
                .baseUrl(paymentProperties.getUrl())
                .requestFactory(factory)
                .requestInterceptor(new PaymentExceptionInterceptor())
                .defaultHeader(HttpHeaders.AUTHORIZATION, createPaymentAuthHeader())
                .build();
    }

    public TossPaymentConfirmResponse confirmPayment(TossPaymentConfirmRequest confirmRequest) {
        return restClient.method(HttpMethod.POST)
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(confirmRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new PaymentConfirmException(getPaymentConfirmErrorCode(response));
                })
                .body(TossPaymentConfirmResponse.class);
    }

    private PaymentConfirmErrorCode getPaymentConfirmErrorCode(final ClientHttpResponse response) throws IOException {
        TossPaymentConfirmFailResponse confirmFailResponse =
                objectMapper.readValue(response.getBody(), TossPaymentConfirmFailResponse.class);
        return PaymentConfirmErrorCode.findByName(confirmFailResponse.getCode());
    }

    private String createPaymentAuthHeader() {
        String raw = paymentProperties.getSecretKey() + BASIC_DELIMITER;
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return AUTH_HEADER_PREFIX + encoded;
    }
}
