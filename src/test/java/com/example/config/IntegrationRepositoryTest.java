package com.example.config;

import com.example.db.repository.*;
import com.example.querydsl.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslConfig.class)
public abstract class IntegrationRepositoryTest extends MySqlIntegrationTest {

    @Autowired
    protected ReservationRepository reservationRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected MovieRepository movieRepository;
    @Autowired
    protected TheaterRepository theaterRepository;
    @Autowired
    protected ScreeningRepository screeningRepository;
    @Autowired
    protected ScreeningSeatRepository screeningSeatRepository;
    @Autowired
    protected MovieRepositoryImpl movieRepositoryImpl;
    @Autowired
    protected PaymentRepository paymentRepository;
}
