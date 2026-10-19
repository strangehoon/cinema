package com.example.db.config;

import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
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

class SampleDataSeederTest {
    @Nested
    @DisplayName("샘플 데이터 초기화 잠금")
    class SeedLock {
        @Test
        @DisplayName("한 인스턴스가 시작 데이터를 넣는 동안 다른 인스턴스의 정기 실행은 건너뛴다")
        void success_startup_and_daily_share_lock() throws Exception {
            // given
            var jdbc = lockTable();
            var entered = new CountDownLatch(1);
            var release = new CountDownLatch(1);
            var failure = new AtomicReference<Throwable>();
            SampleDataWriter firstWriter = mock(SampleDataWriter.class);
            SampleDataWriter secondWriter = mock(SampleDataWriter.class);
            doAnswer(invocation -> {
                entered.countDown();
                if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("초기화 작업 대기 시간 초과");
                return null;
            }).when(firstWriter).seed();
            var first = seeder(jdbc, firstWriter);
            var second = seeder(jdbc, secondWriter);
            Thread running = new Thread(() -> {
                try {
                    first.run(null);
                } catch (Throwable ex) {
                    failure.set(ex);
                }
            });

            // when
            running.start();
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            second.refreshSchedules();
            release.countDown();
            running.join(5_000);

            // then
            assertThat(running.isAlive()).isFalse();
            assertThat(failure.get()).isNull();
            verify(firstWriter).seed();
            verifyNoInteractions(secondWriter);
        }

        @Test
        @DisplayName("기존 작업이 끝나면 다른 인스턴스가 누락된 데이터를 채울 수 있다")
        void success_next_run_after_release() {
            // given
            var jdbc = lockTable();
            SampleDataWriter firstWriter = mock(SampleDataWriter.class);
            SampleDataWriter secondWriter = mock(SampleDataWriter.class);
            var first = seeder(jdbc, firstWriter);
            var second = seeder(jdbc, secondWriter);

            // when
            first.run(null);
            second.refreshSchedules();

            // then
            verify(firstWriter).seed();
            verify(secondWriter).seed();
        }
    }

    private JdbcTemplate lockTable() {
        var dataSource = new DriverManagerDataSource("jdbc:h2:mem:shedlock-" + System.nanoTime() + ";DB_CLOSE_DELAY=-1", "sa", "");
        var jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE shedlock (name VARCHAR(64) PRIMARY KEY, lock_until TIMESTAMP(3) NOT NULL, " +
                "locked_at TIMESTAMP(3) NOT NULL, locked_by VARCHAR(255) NOT NULL)");
        return jdbc;
    }

    private SampleDataSeeder seeder(JdbcTemplate jdbc, SampleDataWriter writer) {
        var provider = new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(jdbc).usingDbTime().build());
        return new SampleDataSeeder(new DefaultLockingTaskExecutor(provider), writer);
    }
}
