CREATE TABLE payments (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER NOT NULL,
    reservation_id INTEGER NOT NULL,
    method         VARCHAR(255) NOT NULL
        CHECK (method IN ('STRIPE', 'PAYPAL', 'INVOICE')),
    amount         DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    status         VARCHAR(255) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'OUTSTANDING')),
    token          VARCHAR(255),
    created_at     TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (reservation_id) REFERENCES reservations (id)
);

CREATE INDEX idx_payments_reservation_id ON payments (reservation_id);
CREATE UNIQUE INDEX idx_payments_token ON payments (token);
