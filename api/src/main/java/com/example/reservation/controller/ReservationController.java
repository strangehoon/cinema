package com.example.reservation.controller;

import com.example.reservation.service.ReservationService;
import com.example.reservation.dto.request.ReservationRequest;
import com.example.reservation.dto.response.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public String reserveSeats(@Valid @ModelAttribute ReservationRequest request, Model model){
        ReservationResponse response = ReservationResponse.from(reservationService.reserveSeats(request.toServiceRequest()));
        model.addAttribute("reservation", response);
        return "checkout";
    }
}