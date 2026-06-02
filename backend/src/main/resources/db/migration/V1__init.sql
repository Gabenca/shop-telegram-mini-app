CREATE TABLE IF NOT EXISTS couples (
    id BIGSERIAL PRIMARY KEY,
    invite_code VARCHAR(6) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    telegram_id BIGINT NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    couple_id BIGINT REFERENCES couples(id)
);

CREATE TABLE IF NOT EXISTS recipes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(2000),
    photo_url VARCHAR(255),
    instructions VARCHAR(4000),
    couple_id BIGINT NOT NULL REFERENCES couples(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT REFERENCES recipes(id),
    name VARCHAR(255),
    quantity DOUBLE PRECISION,
    unit VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS meal_plan_entries (
    id BIGSERIAL PRIMARY KEY,
    date DATE,
    recipe_id BIGINT REFERENCES recipes(id),
    meal_type VARCHAR(255),
    couple_id BIGINT NOT NULL REFERENCES couples(id)
);

CREATE TABLE IF NOT EXISTS shopping_list_items (
    id BIGSERIAL PRIMARY KEY,
    week_start_date DATE,
    ingredient_name VARCHAR(255),
    total_quantity DOUBLE PRECISION,
    unit VARCHAR(255),
    checked BOOLEAN,
    manual BOOLEAN,
    couple_id BIGINT NOT NULL REFERENCES couples(id)
);
