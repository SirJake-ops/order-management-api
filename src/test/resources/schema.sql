CREATE TABLE trading_user(
    id UUID NOT NULL PRIMARY KEY,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    deleted_date TIMESTAMP,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(255) NOT NULL,
    bid_rating DOUBLE PRECISION,
    account_balance DECIMAL(12,2) NOT NULL,
    phone_number VARCHAR(255)
);

CREATE TABLE trading_order (
    id UUID NOT NULL PRIMARY KEY,
    price DECIMAL(18,8) NOT NULL,
    quantity DECIMAL(18,8) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    order_type VARCHAR(50) NOT NULL,
    side VARCHAR(50) NOT NULL,
    filled_quantity DECIMAL(18,8) DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    deleted_date TIMESTAMP
);
