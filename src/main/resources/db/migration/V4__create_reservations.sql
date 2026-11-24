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
    CHECK (check_out > check_in),
    FOREIGN KEY (room_id) REFERENCES rooms (id)
);

CREATE INDEX idx_reservations_room_id ON reservations (room_id);
