CREATE TABLE weekend_rates (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    hotel_id         INTEGER NOT NULL UNIQUE,
    price_multiplier DECIMAL(5, 2) NOT NULL DEFAULT 1.00 CHECK (price_multiplier > 0),
    active           BOOLEAN NOT NULL DEFAULT 1,
    FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
);

CREATE TABLE coupons (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    code           VARCHAR(255) NOT NULL UNIQUE,
    discount_type  VARCHAR(255) NOT NULL
        CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    discount_value DECIMAL(10, 2) NOT NULL CHECK (discount_value >= 0),
    active         BOOLEAN NOT NULL DEFAULT 0,
    valid_from     TIMESTAMP,
    valid_until    TIMESTAMP,
    CHECK (discount_type <> 'PERCENTAGE' OR discount_value <= 100)
);
