package com.example.common;

import com.example.movie.exception.MovieException;
import com.example.payment.exception.PaymentException;
import com.example.reservation.exception.ReservationException;
import com.example.screening.exception.ScreeningException;
import com.example.theater.exception.TheaterException;
import com.example.user.exception.UserException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ApiResponse<Object> bindException(BindException e){
        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MovieException.class)
    public ApiResponse<Object> handleMovieException(ReservationException e) {
        return ApiResponse.of(
                e.getCode(),
                e.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReservationException.class)
    public ApiResponse<Object> handleReservationException(ReservationException e) {
        return ApiResponse.of(
                e.getCode(),
                e.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PaymentException.class)
    public ApiResponse<Object> handlePaymentException(PaymentException e) {
        return ApiResponse.of(
                e.getCode(),
                e.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ScreeningException.class)
    public ApiResponse<Object> handleScreeningException(PaymentException e) {
        return ApiResponse.of(
                e.getCode(),
                e.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TheaterException.class)
    public ApiResponse<Object> handleTheaterException(PaymentException e) {
        return ApiResponse.of(
                e.getCode(),
                e.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserException.class)
    public ApiResponse<Object> handleUserException(PaymentException e) {
        return ApiResponse.of(
                e.getCode(),
                e.getMessage(),
                null
        );
    }
}
