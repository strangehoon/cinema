package com.example.reservation;

import com.example.db.entity.ScreeningSeat;
import com.example.reservation.exception.ReservationException;
import com.example.reservation.service.ReservationPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationPolicyTest {

    @Nested
    @DisplayName("예매 좌석 선택 규칙")
    class ValidateSeats {

        @Test
        @DisplayName("순서와 관계없이 같은 줄의 연속된 최대 3석을 허용한다")
        void success() {
            // given
            List<ScreeningSeat> seats = List.of(seat(1, 3), seat(1, 1), seat(1, 2));

            // when & then
            assertThatCode(() -> ReservationPolicy.validateSeats(seats, 0)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("점유한 좌석과 새 좌석을 합해 정확히 3석이면 허용한다")
        void success_total_limit() {
            // given
            List<ScreeningSeat> seats = List.of(seat(1, 5));

            // when & then
            assertThatCode(() -> ReservationPolicy.validateSeats(seats, 2)).doesNotThrowAnyException();
        }

        @ParameterizedTest
        @MethodSource("com.example.reservation.ReservationPolicyTest#invalidSelections")
        @DisplayName("빈 선택·누적 초과·서로 다른 줄·떨어진 좌석·중복 좌석을 거부한다")
        void fail_invalid_selection(List<ScreeningSeat> seats, int activeCount, String code) {
            // when & then
            assertThatThrownBy(() -> ReservationPolicy.validateSeats(seats, activeCount))
                    .isInstanceOf(ReservationException.class)
                    .extracting("code").isEqualTo(code);
        }
    }

    @Nested
    @DisplayName("결제 대기 만료 시각")
    class ExpiresAt {

        @Test
        @DisplayName("상영까지 충분한 시간이 있으면 5분 동안 점유한다")
        void success() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 10, 15, 12, 0);

            // when
            LocalDateTime expiresAt = ReservationPolicy.expiresAt(now, now.plusHours(1));

            // then
            assertThat(expiresAt).isEqualTo(now.plusMinutes(5));
        }

        @Test
        @DisplayName("5분 이내에 상영이 시작되면 상영 시작 시각에 만료한다")
        void success_before_screening() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 10, 15, 12, 0);

            // when
            LocalDateTime expiresAt = ReservationPolicy.expiresAt(now, now.plusMinutes(2));

            // then
            assertThat(expiresAt).isEqualTo(now.plusMinutes(2));
        }

        @ParameterizedTest
        @MethodSource("com.example.reservation.ReservationPolicyTest#startedScreenings")
        @DisplayName("이미 시작했거나 시작 시각과 같은 회차는 예매할 수 없다")
        void fail_started(LocalDateTime startedAt) {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 10, 15, 12, 0);

            // when & then
            assertThatThrownBy(() -> ReservationPolicy.expiresAt(now, startedAt))
                    .isInstanceOf(ReservationException.class)
                    .hasMessageContaining("상영");
        }
    }

    static Stream<Arguments> invalidSelections() {
        return Stream.of(
                Arguments.of(List.of(), 0, "400_2"),
                Arguments.of(List.of(seat(1, 1), seat(1, 2)), 2, "400_1"),
                Arguments.of(List.of(seat(1, 1), seat(2, 2)), 0, "400_3"),
                Arguments.of(List.of(seat(1, 1), seat(1, 3)), 0, "400_4"),
                Arguments.of(List.of(seat(1, 1), seat(1, 1)), 0, "400_2"),
                Arguments.of(List.of(seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4)), 0, "400_1")
        );
    }

    static Stream<LocalDateTime> startedScreenings() {
        return Stream.of(LocalDateTime.of(2026, 10, 15, 12, 0), LocalDateTime.of(2026, 10, 15, 11, 59));
    }

    private static ScreeningSeat seat(int row, int col) {
        return ScreeningSeat.builder().row(row).col(col).build();
    }
}
