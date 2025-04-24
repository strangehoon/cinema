package com.example.config;

import com.example.movie.service.MovieService;
import com.example.reservation.service.ReservationService;
import com.example.movie.controller.MovieController;
import com.example.reservation.controller.ReservationController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest({
        MovieController.class,
        ReservationController.class
})
public abstract class IntegrationControllerSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected MovieService movieService;

    @MockitoBean
    protected ReservationService reservationService;
}
