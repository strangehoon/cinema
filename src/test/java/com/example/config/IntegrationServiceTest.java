package com.example.config;

import com.example.db.repository.*;
import com.example.reservation.service.ReservationService;
import com.example.reservation.service.ReservationValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import({EmbeddedRedisConfig.class, TestObjectMapperConfig.class})
@SpringBootTest
public abstract class IntegrationServiceTest extends MySqlIntegrationTest {

    @Autowired
    protected MovieRepository movieRepository;

    @Autowired
    protected TheaterRepository theaterRepository;

    @Autowired
    protected ReservationValidator reservationValidator;

    @Autowired
    protected ScreeningRepository screeningRepository;

    @Autowired
    protected ReservationService reservationService;

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected ScreeningSeatRepository screeningSeatRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PaymentRepository paymentRepository;

    @Autowired
    protected BookingOrderRepository bookingOrderRepository;

    @PersistenceContext
    protected EntityManager em;
}
