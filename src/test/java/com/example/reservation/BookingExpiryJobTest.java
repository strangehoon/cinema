package com.example.reservation;

import com.example.reservation.service.BookingExpiryJob;
import com.example.reservation.service.BookingService;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookingExpiryJobTest {
    @Nested
    @DisplayName("예매 만료 작업 잠금")
    class Lock {
        @Test
        @DisplayName("한 인스턴스가 만료 처리 중이면 다른 인스턴스는 중복 실행하지 않는다")
        void success_single_execution_during_overlap() throws Exception {
            // given
            var jdbc = lockTable();
            BookingService firstService = mock(BookingService.class);
            BookingService secondService = mock(BookingService.class);
            var entered = new CountDownLatch(1);
            var release = new CountDownLatch(1);
            var failure = new AtomicReference<Throwable>();
            doAnswer(invocation -> {
                entered.countDown();
                if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("만료 처리 대기 시간 초과");
                return null;
            }).when(firstService).expireDue();
            var first = job(jdbc, firstService);
            var second = job(jdbc, secondService);
            Thread running = new Thread(() -> {
                try {
                    first.run();
                } catch (Throwable ex) {
                    failure.set(ex);
                }
            });

            // when
            running.start();
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            second.run();
            release.countDown();
            running.join(5_000);

            // then
            assertThat(running.isAlive()).isFalse();
            assertThat(failure.get()).isNull();
            verify(firstService).expireDue();
            verifyNoInteractions(secondService);
            second.run();
            verify(secondService).expireDue();
        }

        @Test
        @DisplayName("처리 실패 후 잠금이 풀려 다음 실행에서 다시 시도한다")
        void fail_then_retry() {
            // given
            var jdbc = lockTable();
            BookingService firstService = mock(BookingService.class);
            BookingService secondService = mock(BookingService.class);
            doThrow(new IllegalStateException("DB 오류")).when(firstService).expireDue();

            // when & then
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> job(jdbc, firstService).run())
                    .isInstanceOf(IllegalStateException.class);
            job(jdbc, secondService).run();
            verify(secondService).expireDue();
        }
    }

    private JdbcTemplate lockTable() {
        var dataSource = new DriverManagerDataSource("jdbc:h2:mem:expiry-lock-" + System.nanoTime() + ";DB_CLOSE_DELAY=-1", "sa", "");
        var jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE shedlock (name VARCHAR(64) PRIMARY KEY, lock_until TIMESTAMP(3) NOT NULL, " +
                "locked_at TIMESTAMP(3) NOT NULL, locked_by VARCHAR(255) NOT NULL)");
        return jdbc;
    }

    private BookingExpiryJob job(JdbcTemplate jdbc, BookingService bookings) {
        var provider = new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(jdbc).usingDbTime().build());
        return new BookingExpiryJob(new DefaultLockingTaskExecutor(provider), bookings);
    }
}
