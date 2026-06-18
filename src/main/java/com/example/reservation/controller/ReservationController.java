package com.example.reservation.controller;

import com.example.reservation.service.ReservationService;
import com.example.reservation.dto.request.ReservationCreateRequest;
import com.example.reservation.dto.response.ReservationCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public String createReserve(@Valid @ModelAttribute ReservationCreateRequest request, Model model){
        ReservationCreateResponse response = ReservationCreateResponse.from(reservationService.createReserve(request.toServiceRequest()));
        model.addAttribute("reservation", response);
        return "checkout";
    }
}