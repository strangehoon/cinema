ALTER TABLE booking_seats ADD COLUMN booking_order_id BIGINT;

UPDATE booking_seats s
JOIN payments p ON s.payment_id = p.id
SET s.booking_order_id = p.booking_order_id;

ALTER TABLE booking_seats
    MODIFY COLUMN booking_order_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_booking_seat_order FOREIGN KEY (booking_order_id) REFERENCES booking_orders (id);

ALTER TABLE booking_seats DROP FOREIGN KEY fk_booking_seat_payment;
ALTER TABLE booking_seats DROP COLUMN payment_id;
CREATE INDEX idx_booking_seat_order ON booking_seats (booking_order_id);
