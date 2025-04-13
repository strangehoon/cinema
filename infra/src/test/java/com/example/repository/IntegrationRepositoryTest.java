package com.example.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import(TestJpaConfig.class)
public abstract class IntegrationRepositoryTest {

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
}
