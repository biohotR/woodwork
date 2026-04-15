-- liquibase formatted sql

-- changeset biohotR:8
-- Insert Categories (Using static UUIDs so we can reference them below)
INSERT INTO category (id, name) VALUES
('a1111111-1111-1111-1111-111111111111', 'Power Tools'),
('b2222222-2222-2222-2222-222222222222', 'Hand Tools'),
('c3333333-3333-3333-3333-333333333333', 'Safety Gear');


-- Insert Products (Power Tools)
INSERT INTO product (id, name, description, price, category_id) VALUES
(gen_random_uuid(), 'DeWalt 20V Max Table Saw', 'Compact jobsite table saw with 8-1/4 inch blade', 399.00, 'a1111111-1111-1111-1111-111111111111'),
(gen_random_uuid(), 'Makita Router', '2-1/4 HP compact router kit', 149.99, 'a1111111-1111-1111-1111-111111111111'),
(gen_random_uuid(), 'Bosch Jig Saw', 'Top-handle orbital jig saw with carrying case', 129.00, 'a1111111-1111-1111-1111-111111111111'),
(gen_random_uuid(), 'Milwaukee Cordless Drill', 'M18 1/2 in. brushless cordless drill/driver', 139.50, 'a1111111-1111-1111-1111-111111111111');

-- Insert Products (Hand Tools)
INSERT INTO product (id, name, description, price, category_id) VALUES
(gen_random_uuid(), 'Stanley Block Plane', 'Low angle block plane for end-grain', 45.00, 'b2222222-2222-2222-2222-222222222222'),
(gen_random_uuid(), 'Irwin Clamps Set', '4-piece quick-grip bar clamp set', 35.99, 'b2222222-2222-2222-2222-222222222222'),
(gen_random_uuid(), 'Narex Woodworking Chisels', 'Set of 4 bevel edge chisels', 65.00, 'b2222222-2222-2222-2222-222222222222');

-- Insert Products (Safety Gear)
INSERT INTO product (id, name, description, price, category_id) VALUES
(gen_random_uuid(), '3M Safety Glasses', 'Anti-fog protective eyewear', 12.50, 'c3333333-3333-3333-3333-333333333333'),
(gen_random_uuid(), '3M Respirator Mask', 'Half facepiece reusable respirator', 29.99, 'c3333333-3333-3333-3333-333333333333'),
(gen_random_uuid(), 'Howard Leight Earmuffs', 'Noise-blocking hearing protection', 24.00, 'c3333333-3333-3333-3333-333333333333');
