package com.example.common;

import com.example.reservation.exception.ReservationException;
import com.example.screening.exception.ScreeningException;
import com.example.theater.exception.TheaterException;
import com.example.user.exception.UserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(RestClientException.class)
    public ApiResponse<Object> handlePaymentGatewayException(RestClientException e) {
        return ApiResponse.of(HttpStatus.BAD_GATEWAY, "결제사 응답을 확인하지 못했습니다. 잠시 후 예매 내역을 확인해 주세요.", null);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<Object> handleIllegalStateException(IllegalStateException e) {
        return ApiResponse.of(HttpStatus.CONFLICT, e.getMessage(), null);
    }

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

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(SecurityException.class)
    public ApiResponse<Object> handleSecurityException(SecurityException e) {
        return ApiResponse.of(HttpStatus.FORBIDDEN, e.getMessage(), null);
    }

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ApiResponse<Object>> handleReservationException(ReservationException e) {
        HttpStatus status = e.getCode().startsWith("409") ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ApiResponse.of(
                e.getCode(),
                e.getMessage(),
                null
        ));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ScreeningException.class)
    public ApiResponse<Object> handleScreeningException(ScreeningException e) {
        return ApiResponse.of(
                e.getCode(),
                e.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TheaterException.class)
    public ApiResponse<Object> handleTheaterException(TheaterException e) {
        return ApiResponse.of(
                e.getCode(),
                e.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserException.class)
    public ApiResponse<Object> handleUserException(UserException e) {
        return ApiResponse.of(
                e.getCode(),
                e.getMessage(),
                null
        );
    }
}
