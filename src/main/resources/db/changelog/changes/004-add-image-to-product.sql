-- liquibase formatted sql

-- changeset biohotR:7
ALTER TABLE product ADD COLUMN image_url VARCHAR(255);
