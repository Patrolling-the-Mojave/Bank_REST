CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_users_username ON users(username);

CREATE TABLE IF NOT EXISTS roles(
    id UUID PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE INDEX idx_roles_name ON roles(name);

CREATE TABLE IF NOT EXISTS user_roles(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS cards (
    id UUID PRIMARY KEY,
    encrypted_number VARCHAR(255) NOT NULL UNIQUE,
    owner_id UUID NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    balance NUMERIC(19, 4) NOT NULL DEFAULT 0.0,
    CONSTRAINT fk_cards_owner_id FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE INDEX idx_cards_owner_id ON cards(owner_id);


CREATE TABLE IF NOT EXISTS transactions(
    id UUID PRIMARY KEY,
    from_card_id UUID NOT NULL,
    to_card_id UUID NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trans_from FOREIGN KEY (from_card_id) REFERENCES cards (id),
    CONSTRAINT fk_trans_to FOREIGN KEY (to_card_id) REFERENCES cards (id)
);

