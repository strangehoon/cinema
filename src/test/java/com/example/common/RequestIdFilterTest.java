package com.example.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {
    private final RequestIdFilter filter = new RequestIdFilter();

    @Nested
    @DisplayName("요청 ID 처리")
    class RequestId {
        @Test
        @DisplayName("요청마다 ID를 응답 헤더와 MDC에 설정하고 요청 종료 후 제거한다")
        void success() throws ServletException, IOException {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = (servletRequest, servletResponse) -> {
                assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNotBlank();
                assertThat(((MockHttpServletResponse) servletResponse).getHeader(RequestIdFilter.HEADER_NAME))
                        .isEqualTo(MDC.get(RequestIdFilter.MDC_KEY));
            };

            // when
            filter.doFilter(request, response, chain);

            // then
            String requestId = response.getHeader(RequestIdFilter.HEADER_NAME);
            assertThat(requestId).isNotBlank();
            assertThat(java.util.UUID.fromString(requestId)).isNotNull();
            assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
        }

        @Test
        @DisplayName("요청 처리 중 예외가 나도 MDC를 제거한다")
        void fail_chain_exception() throws ServletException, IOException {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = (servletRequest, servletResponse) -> {
                throw new ServletException("request failed");
            };

            // when & then
            try {
                filter.doFilter(request, response, chain);
            } catch (ServletException ignored) {
                assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
                return;
            }
            throw new AssertionError("Expected ServletException");
        }
    }
}
