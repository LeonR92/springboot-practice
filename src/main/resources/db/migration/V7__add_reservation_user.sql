ALTER TABLE reservations ADD COLUMN user_id INTEGER REFERENCES users (id);

CREATE INDEX idx_reservations_user_id ON reservations (user_id);
