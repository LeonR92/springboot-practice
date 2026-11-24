CREATE TABLE reservations (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    room_id     INTEGER NOT NULL,
    guest_name  VARCHAR(255) NOT NULL,
    guest_email VARCHAR(255) NOT NULL,
    check_in    DATE NOT NULL,
    check_out   DATE NOT NULL,
    status      VARCHAR(255) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    lock_until  TIMESTAMP NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    coupon_code VARCHAR(255),
    CHECK (check_out > check_in),
    FOREIGN KEY (room_id) REFERENCES rooms (id)
);

CREATE INDEX idx_reservations_room_id ON reservations (room_id);
