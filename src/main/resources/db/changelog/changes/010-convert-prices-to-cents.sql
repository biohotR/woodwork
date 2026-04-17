-- liquibase formatted sql
-- changeset radusilvestru:13

ALTER TABLE product ALTER COLUMN price TYPE INTEGER USING (price * 100);

ALTER TABLE order_items ALTER COLUMN purchase_price TYPE INTEGER USING (purchase_price * 100);

ALTER TABLE orders ALTER COLUMN total_amount TYPE BIGINT USING (total_amount * 100);
