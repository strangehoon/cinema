package com.example.config;

import com.example.service.MovieService;
import com.example.service.ReservationService;
import com.example.controller.MovieController;
import com.example.controller.ReservationController;
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
