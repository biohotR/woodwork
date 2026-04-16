-- liquibase formatted sql
-- changeset radusilvestru:11

UPDATE product
SET stock_quantity = 50
WHERE stock_quantity IS NULL;
