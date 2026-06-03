-- Составные индексы для горячих запросов
CREATE INDEX IF NOT EXISTS idx_recipes_couple_id ON recipes (couple_id);
CREATE INDEX IF NOT EXISTS idx_meal_plan_entries_date_couple ON meal_plan_entries (date, couple_id);
CREATE INDEX IF NOT EXISTS idx_shopping_list_week_couple ON shopping_list_items (week_start_date, couple_id);
CREATE INDEX IF NOT EXISTS idx_ingredients_recipe_id ON ingredients (recipe_id);
CREATE INDEX IF NOT EXISTS idx_meal_plan_entry_dishes_entry_id ON meal_plan_entry_dishes (meal_plan_entry_id);
CREATE INDEX IF NOT EXISTS idx_users_couple_id ON users (couple_id);
