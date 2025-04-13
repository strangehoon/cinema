package com.example.controller;

import com.example.service.ReservationService;
import com.example.dto.request.ReservationRequest;
import com.example.dto.response.ApiResponse;
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
    public ApiResponse<ReservationResponse> reserveSeats(@Valid @RequestBody ReservationRequest request){
        return ApiResponse.ok(ReservationResponse.from(reservationService.reserveSeats(request.toServiceRequest())));
    }
}