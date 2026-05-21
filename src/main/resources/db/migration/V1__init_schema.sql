-- Enable PostGIS extension (required for geometry columns)
CREATE EXTENSION IF NOT EXISTS postgis;

-- ============================================================
-- Users
-- ============================================================
CREATE TABLE IF NOT EXISTS app_user (
                                        id       BIGSERIAL PRIMARY KEY,
                                        name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT uq_user_email UNIQUE (email)
    );

CREATE INDEX IF NOT EXISTS idx_user_email ON app_user (email);

-- Roles join table (maps to @ElementCollection in User entity)
CREATE TABLE IF NOT EXISTS app_user_roles (
                                              app_user_id BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    roles       VARCHAR(50) NOT NULL,
    PRIMARY KEY (app_user_id, roles)
    );

-- ============================================================
-- Rider
-- ============================================================
CREATE TABLE IF NOT EXISTS rider (
                                     id      BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT           NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    rating  DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    CONSTRAINT uq_rider_user UNIQUE (user_id)
    );

-- ============================================================
-- Driver
-- ============================================================
CREATE TABLE IF NOT EXISTS driver (
                                      id               BIGSERIAL PRIMARY KEY,
                                      user_id          BIGINT                NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    rating           DOUBLE PRECISION      NOT NULL DEFAULT 0.0,
    vehicle_id       VARCHAR(255),
    is_available     BOOLEAN               NOT NULL DEFAULT TRUE,
    current_location geometry(Point, 4326),
    CONSTRAINT uq_driver_user UNIQUE (user_id)
    );

CREATE INDEX IF NOT EXISTS idx_driver_is_available  ON driver (is_available);
CREATE INDEX IF NOT EXISTS idx_driver_location       ON driver USING GIST (current_location);

-- ============================================================
-- Ride request
-- ============================================================
CREATE TABLE IF NOT EXISTS ride_request (
                                            id                   BIGSERIAL PRIMARY KEY,
                                            rider_id             BIGINT           NOT NULL REFERENCES rider (id),
    pickup_location      geometry(Point, 4326),
    destination_location geometry(Point, 4326),
    ride_request_status  VARCHAR(50),
    payment_method       VARCHAR(50),
    fare                 DOUBLE PRECISION,
    created_time         TIMESTAMP        NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_ride_request_rider ON ride_request (rider_id);

-- ============================================================
-- Ride
-- ============================================================
CREATE TABLE IF NOT EXISTS ride (
                                    id                   BIGSERIAL PRIMARY KEY,
                                    rider_id             BIGINT           NOT NULL REFERENCES rider (id),
    driver_id            BIGINT           NOT NULL REFERENCES driver (id),
    pickup_location      geometry(Point, 4326),
    destination_location geometry(Point, 4326),
    ride_status          VARCHAR(50),
    payment_method       VARCHAR(50),
    otp                  VARCHAR(10),
    fare                 DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    started_at           TIMESTAMP,
    ended_at             TIMESTAMP,
    created_time         TIMESTAMP        NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_ride_rider  ON ride (rider_id);
CREATE INDEX IF NOT EXISTS idx_ride_driver ON ride (driver_id);
CREATE INDEX IF NOT EXISTS idx_ride_status ON ride (ride_status);

-- ============================================================
-- Wallet
-- ============================================================
CREATE TABLE IF NOT EXISTS wallet (
                                      id      BIGSERIAL PRIMARY KEY,
                                      user_id BIGINT           NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    CONSTRAINT uq_wallet_user UNIQUE (user_id)
    );

-- ============================================================
-- Wallet transaction
-- ============================================================
CREATE TABLE IF NOT EXISTS wallet_transaction (
                                                  id                 BIGSERIAL PRIMARY KEY,
                                                  wallet_id          BIGINT           NOT NULL REFERENCES wallet (id),
    ride_id            BIGINT REFERENCES ride (id),
    transaction_id     VARCHAR(255),
    amount             DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    transaction_type   VARCHAR(50),
    transaction_method VARCHAR(50),
    created_at         TIMESTAMP        NOT NULL DEFAULT now(),
    CONSTRAINT uq_wallet_txn_id UNIQUE (transaction_id)
    );

CREATE INDEX IF NOT EXISTS idx_wallet_txn_wallet ON wallet_transaction (wallet_id);

-- ============================================================
-- Payment
-- ============================================================
CREATE TABLE IF NOT EXISTS payment (
                                       id             BIGSERIAL PRIMARY KEY,
                                       ride_id        BIGINT           NOT NULL REFERENCES ride (id),
    payment_status VARCHAR(50),
    amount         DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    CONSTRAINT uq_payment_ride UNIQUE (ride_id)
    );

-- ============================================================
-- Rating
-- ============================================================
CREATE TABLE IF NOT EXISTS rating (
                                      id             BIGSERIAL PRIMARY KEY,
                                      ride_id        BIGINT  NOT NULL REFERENCES ride (id),
    driver_id      BIGINT  NOT NULL REFERENCES driver (id),
    rider_id       BIGINT  NOT NULL REFERENCES rider (id),
    driver_rating  INTEGER,
    rider_rating   INTEGER
    );

CREATE INDEX IF NOT EXISTS idx_rating_ride   ON rating (ride_id);
CREATE INDEX IF NOT EXISTS idx_rating_driver ON rating (driver_id);