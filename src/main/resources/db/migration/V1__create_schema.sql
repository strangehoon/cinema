CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20), email VARCHAR(190) UNIQUE, password_hash VARCHAR(100),
    created_at DATETIME(6), created_by BIGINT, updated_at DATETIME(6), updated_by BIGINT
);

CREATE TABLE theaters (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(30),
    created_at DATETIME(6), created_by BIGINT, updated_at DATETIME(6), updated_by BIGINT
);

CREATE TABLE movies (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(30), rating VARCHAR(20), released_date DATE,
    thumbnail_image VARCHAR(50), running_time_min INT NOT NULL, genre VARCHAR(20),
    created_at DATETIME(6), created_by BIGINT, updated_at DATETIME(6), updated_by BIGINT
);

CREATE TABLE screening_seats (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    seat_row INT NOT NULL, seat_col INT NOT NULL, theater_id BIGINT NOT NULL,
    created_at DATETIME(6), created_by BIGINT, updated_at DATETIME(6), updated_by BIGINT,
    CONSTRAINT uk_theater_seat UNIQUE (theater_id, seat_row, seat_col),
    INDEX idx_seat_theater (theater_id)
);

CREATE TABLE screenings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    date DATE, started_at DATETIME(6), ended_at DATETIME(6),
    movie_id BIGINT NOT NULL, theater_id BIGINT NOT NULL,
    created_at DATETIME(6), created_by BIGINT, updated_at DATETIME(6), updated_by BIGINT,
    CONSTRAINT uk_theater_start UNIQUE (theater_id, started_at),
    INDEX idx_screening_movie (movie_id), INDEX idx_screening_date (date)
);

CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_key VARCHAR(40), type VARCHAR(20), order_id VARCHAR(100) NOT NULL UNIQUE,
    order_name VARCHAR(50), total_amount BIGINT, status VARCHAR(20), method VARCHAR(20),
    requested_at DATETIME(6), approved_at DATETIME(6), expires_at DATETIME(6),
    user_id BIGINT,
    created_at DATETIME(6), created_by BIGINT, updated_at DATETIME(6), updated_by BIGINT,
    INDEX idx_payment_user (user_id), INDEX idx_payment_expiry (status, expires_at)
);

CREATE TABLE reservations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(20), price INT NOT NULL,
    screening_seat_id BIGINT NOT NULL, screening_id BIGINT NOT NULL,
    payment_id BIGINT, user_id BIGINT,
    created_at DATETIME(6), created_by BIGINT, updated_at DATETIME(6), updated_by BIGINT,
    CONSTRAINT uk_screening_seat UNIQUE (screening_id, screening_seat_id),
    INDEX idx_reservation_payment (payment_id), INDEX idx_reservation_user_screening (user_id, screening_id)
);

CREATE TABLE booking_seats (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL, screening_id BIGINT, starts_at DATETIME(6),
    seat_label VARCHAR(255), price INT NOT NULL,
    CONSTRAINT fk_booking_seat_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
    INDEX idx_booking_seat_payment (payment_id)
);
