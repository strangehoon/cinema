CREATE TABLE booking_orders (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL UNIQUE,
    order_name VARCHAR(50), total_amount BIGINT, status VARCHAR(20),
    expires_at DATETIME(6), user_id BIGINT,
    created_at DATETIME(6), created_by BIGINT, updated_at DATETIME(6), updated_by BIGINT,
    INDEX idx_booking_order_user (user_id),
    INDEX idx_booking_order_expiry (status, expires_at)
);

INSERT INTO booking_orders (id, order_id, order_name, total_amount, status, expires_at,
                            user_id, created_at, created_by, updated_at, updated_by)
SELECT id, order_id, order_name, total_amount, status, expires_at,
       user_id, created_at, created_by, updated_at, updated_by
FROM payments;

ALTER TABLE payments ADD COLUMN booking_order_id BIGINT;
UPDATE payments SET booking_order_id = id;
ALTER TABLE payments MODIFY COLUMN booking_order_id BIGINT NOT NULL,
    ADD CONSTRAINT uk_payment_booking_order UNIQUE (booking_order_id),
    ADD CONSTRAINT fk_payment_booking_order FOREIGN KEY (booking_order_id) REFERENCES booking_orders (id);

ALTER TABLE payments
    DROP COLUMN order_id,
    DROP COLUMN order_name,
    DROP COLUMN total_amount,
    DROP COLUMN status,
    DROP COLUMN expires_at,
    DROP COLUMN user_id;
