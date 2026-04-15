-- liquibase formatted sql

-- changeset biohotR:6
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    address TEXT,
    phone_number VARCHAR(20),
    user_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_user_profile FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
