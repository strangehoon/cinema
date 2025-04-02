package com.example;

import com.example.dto.request.ReservationRequest;
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
    public String reserveSeats(@Valid @RequestBody ReservationRequest request){
        return reservationService.reserveSeats(request.toServiceRequest());
    }
}
