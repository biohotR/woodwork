-- liquibase formatted sql
-- changeset radusilvestru:9

ALTER TABLE product
ADD COLUMN stock_quantity INT NOT NULL DEFAULT 0;

UPDATE product
SET stock_quantity = 50;
