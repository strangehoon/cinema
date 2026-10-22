package com.example.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Nested
    @DisplayName("예기치 않은 서버 오류")
    class UnexpectedError {
        @Test
        @DisplayName("요청 ID를 포함해 오류를 기록하고 내부 정보를 응답에 노출하지 않는다")
        void fail_unexpected_error(CapturedOutput output) {
            // given
            MDC.put("requestId", "request-test-123");
            IllegalStateException exception = new IllegalStateException("private-detail");

            // when
            var response = handler.handleUnexpectedException(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
            assertThat(output).contains("Unhandled server error");
        }
    }

    @Nested
    @DisplayName("예상된 클라이언트 오류")
    class ClientError {
        @Test
        @DisplayName("4xx 예외는 오류 로그로 기록하지 않는다")
        void fail_bad_request(CapturedOutput output) {
            // given
            IllegalArgumentException exception = new IllegalArgumentException("잘못된 요청");

            // when
            var response = handler.handleIllegalArgumentException(exception);

            // then
            assertThat(response.getCode()).isEqualTo("400");
            assertThat(output).doesNotContain("Unhandled server error");
        }
    }

    @Nested
    @DisplayName("결제사 연동 오류")
    class PaymentGatewayError {
        @Test
        @DisplayName("결제사 오류는 장애 로그로 기록하고 상세 오류를 응답에 노출하지 않는다")
        void fail_gateway(CapturedOutput output) {
            // given
            RestClientException exception = new RestClientException("gateway internal detail");

            // when
            ApiResponse<Object> response = handler.handlePaymentGatewayException(exception);

            // then
            assertThat(response.getCode()).isEqualTo("502");
            assertThat(response.getMessage()).doesNotContain("gateway internal detail");
            assertThat(output).contains("Payment gateway request failed");
        }
    }
}
