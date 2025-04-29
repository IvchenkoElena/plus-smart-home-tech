CREATE SCHEMA IF NOT EXISTS delivery;

CREATE TABLE IF NOT EXISTS delivery.addresses (
    address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country VARCHAR(100),
    city VARCHAR(100),
    street VARCHAR(100),
    house VARCHAR(100),
    flat VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS delivery.deliveries (
    delivery_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_address_id UUID NOT NULL REFERENCES delivery.addresses(address_id) ON DELETE CASCADE,
    to_address_id UUID NOT NULL REFERENCES delivery.addresses(address_id) ON DELETE CASCADE,
    order_id UUID NOT NULL,
    delivery_state VARCHAR(10)
);