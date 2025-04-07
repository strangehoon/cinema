package com.example.presentation;

import com.example.ReservationService;
import com.example.dto.request.ReservationRequest;
import com.example.dto.response.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ReservationResponse reserveSeats(@Valid @RequestBody ReservationRequest request){
        return ReservationResponse.from(reservationService.reserveSeats(request.toServiceRequest()));
    }
}