CREATE TABLE IF NOT EXISTS trading_user (
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

INSERT INTO trading_user (
    id,
    created_date,
    last_modified_date,
    deleted_date,
    username,
    password,
    first_name,
    last_name,
    email,
    role,
    bid_rating,
    account_balance,
    phone_number
)
SELECT
    (
        substring(md5(au.username), 1, 8) || '-' ||
        substring(md5(au.username), 9, 4) || '-' ||
        substring(md5(au.username), 13, 4) || '-' ||
        substring(md5(au.username), 17, 4) || '-' ||
        substring(md5(au.username), 21, 12)
    )::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL,
    au.username,
    au.password,
    au.first_name,
    au.last_name,
    au.email,
    au.role,
    NULL,
    0.00,
    NULL
FROM application_user au
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'application_user')
  AND NOT EXISTS (SELECT 1 FROM trading_user tu WHERE tu.username = au.username);
