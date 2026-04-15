-- liquibase formatted sql
-- changeset biohotR:1

CREATE TABLE category (
    id UUID PRIMARY KEY,
    name varchar(255) NOT NULL
);

CREATE TABLE product (
    id UUID PRIMARY KEY,
    name varchar(255) NOT NULL,
    description text,
    price double precision,
    category_id UUID,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id)
);

