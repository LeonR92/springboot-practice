CREATE TABLE room_types (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        VARCHAR(255) NOT NULL UNIQUE,
    tier        VARCHAR(255) NOT NULL
        CHECK (tier IN ('STANDARD', 'SUPERIOR', 'DELUXE', 'SUITE')),
    base_price  DECIMAL(10, 2) NOT NULL CHECK (base_price >= 0),
    capacity    INTEGER NOT NULL CHECK (capacity >= 1),
    description VARCHAR(255)
);

CREATE TABLE rooms (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    hotel_id     INTEGER NOT NULL,
    room_type_id INTEGER NOT NULL,
    room_number  VARCHAR(50) NOT NULL,
    floor        INTEGER,
    status       VARCHAR(255) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'OUT_OF_SERVICE')),
    UNIQUE (hotel_id, room_number),
    FOREIGN KEY (hotel_id)     REFERENCES hotels (id) ON DELETE CASCADE,
    FOREIGN KEY (room_type_id) REFERENCES room_types (id) ON DELETE RESTRICT
);

CREATE INDEX idx_rooms_room_type_id ON rooms (room_type_id);
