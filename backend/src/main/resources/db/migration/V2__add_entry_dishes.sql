CREATE TABLE IF NOT EXISTS meal_plan_entry_dishes (
    id BIGSERIAL PRIMARY KEY,
    meal_plan_entry_id BIGINT NOT NULL REFERENCES meal_plan_entries(id) ON DELETE CASCADE,
    recipe_id BIGINT REFERENCES recipes(id),
    manual_name VARCHAR(255),
    manual_quantity DOUBLE PRECISION,
    manual_unit VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_recipe_or_manual CHECK (
        (recipe_id IS NOT NULL AND manual_name IS NULL) OR
        (recipe_id IS NULL AND manual_name IS NOT NULL)
    )
);

CREATE INDEX idx_entry_dishes_entry_id ON meal_plan_entry_dishes(meal_plan_entry_id);

-- Migrate existing data: convert recipe_id from meal_plan_entries to dishes
INSERT INTO meal_plan_entry_dishes (meal_plan_entry_id, recipe_id, sort_order)
SELECT id, recipe_id, 0
FROM meal_plan_entries
WHERE recipe_id IS NOT NULL;

-- Remove recipe_id from meal_plan_entries
ALTER TABLE meal_plan_entries DROP COLUMN IF EXISTS recipe_id;