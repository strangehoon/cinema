package com.example.payment.controller;

import com.example.payment.exception.PaymentConfirmException;
import com.example.payment.exception.PaymentConfirmErrorCode;
import com.example.interceptor.PaymentExceptionInterceptor;
import com.example.payment.dto.request.TossPaymentConfirmRequest;
import com.example.payment.dto.response.TossPaymentConfirmFailResponse;
import com.example.payment.dto.response.TossPaymentConfirmResponse;
import com.example.payment.service.PaymentValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${psp.toss.secret-key}")
    private String secretKey;

    @Value("${psp.toss.url}")
    private String url;

    private static final int CONNECT_TIMEOUT_SECONDS = 1;
    private static final int READ_TIMEOUT_SECONDS = 30;
    private static final String BASIC_DELIMITER = ":";
    private static final String AUTH_HEADER_PREFIX = "Basic ";
    private static final String PAYMENT_CONFIRM_URI = "/v1/payments/confirm";

    private final ObjectMapper objectMapper;
    private RestClient restClient;
    private final PaymentValidator paymentValidator;

    @PostConstruct
    private void init() {
        this.restClient = createRestClient();
    }

    private RestClient createRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_SECONDS * 1000);
        factory.setReadTimeout(READ_TIMEOUT_SECONDS * 1000);

        return RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .requestInterceptor(new PaymentExceptionInterceptor())
                .defaultHeader(HttpHeaders.AUTHORIZATION, createPaymentAuthHeader())
                .build();
    }

    public TossPaymentConfirmResponse confirmPayment(TossPaymentConfirmRequest confirmRequest) {

        paymentValidator.validate(confirmRequest.toServiceRequest());
        return restClient.method(HttpMethod.POST)
                .uri(PAYMENT_CONFIRM_URI)
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
        String raw = secretKey + BASIC_DELIMITER;
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return AUTH_HEADER_PREFIX + encoded;
    }
}
