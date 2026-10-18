package com.example.reservation.service;

import com.example.db.entity.ScreeningSeat;
import com.example.reservation.exception.ReservationException;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.reservation.exception.ReservationErrorCode.*;

public final class ReservationPolicy {
    public static final int MAX_SEATS = 3;
    public static final int HOLD_MINUTES = 5;

    private ReservationPolicy() {
    }

    public static void validateSeats(List<ScreeningSeat> seats, int activeCount) {
        if (seats == null || seats.isEmpty() || seats.stream().anyMatch(seat -> seat == null)) {
            throw new ReservationException(INVALID_SEAT);
        }
        if (activeCount + seats.size() > MAX_SEATS) {
            throw new ReservationException(MAX_SEAT_LIMIT_EXCEEDED);
        }
        int row = seats.getFirst().getRow();
        if (seats.stream().anyMatch(seat -> seat.getRow() != row)) {
            throw new ReservationException(NOT_SAME_ROW);
        }
        List<Integer> columns = seats.stream().map(ScreeningSeat::getCol).sorted().toList();
        if (columns.stream().distinct().count() != columns.size()) {
            throw new ReservationException(INVALID_SEAT);
        }
        for (int i = 1; i < columns.size(); i++) {
            if (columns.get(i) != columns.get(i - 1) + 1) {
                throw new ReservationException(NOT_ADJACENT_SEAT);
            }
        }
    }

    public static LocalDateTime expiresAt(LocalDateTime now, LocalDateTime startedAt) {
        if (!startedAt.isAfter(now)) {
            throw new ReservationException(SCREENING_ALREADY_STARTED);
        }
        LocalDateTime deadline = now.plusMinutes(HOLD_MINUTES);
        return startedAt.isBefore(deadline) ? startedAt : deadline;
    }
}
