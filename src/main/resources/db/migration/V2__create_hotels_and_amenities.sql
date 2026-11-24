CREATE TABLE amenities (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    category    VARCHAR(255) NOT NULL
        CHECK (category IN ('GENERAL', 'WELLNESS', 'FOOD', 'TRANSPORT')),
    icon        VARCHAR(255),
    is_free     BOOLEAN NOT NULL DEFAULT 1
);

CREATE TABLE hotels (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    name           VARCHAR(255) NOT NULL,
    country        VARCHAR(255) NOT NULL,
    city           VARCHAR(255) NOT NULL,
    street         VARCHAR(255),
    latitude       DOUBLE,
    longitude      DOUBLE,
    check_in_time  TIME NOT NULL,
    check_out_time TIME NOT NULL
);

CREATE TABLE hotel_amenities (
    hotel_id   INTEGER NOT NULL,
    amenity_id INTEGER NOT NULL,
    PRIMARY KEY (hotel_id, amenity_id),
    FOREIGN KEY (hotel_id)   REFERENCES hotels (id)    ON DELETE CASCADE,
    FOREIGN KEY (amenity_id) REFERENCES amenities (id) ON DELETE CASCADE
);

CREATE INDEX idx_hotel_amenities_amenity_id ON hotel_amenities (amenity_id);
