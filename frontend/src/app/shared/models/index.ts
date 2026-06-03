export interface User {
  id: number;
  telegramId: number;
  username: string;
}

export interface Couple {
  id: number;
  inviteCode: string;
  users: User[];
}

export interface Recipe {
  id: number;
  name: string;
  description: string;
  photoUrl: string;
  instructions: string;
  ingredients: Ingredient[];
  createdAt: string;
  updatedAt: string;
}

export interface Ingredient {
  id: number;
  name: string;
  quantity: number;
  unit: 'GRAM' | 'MILLILITER' | 'PIECE';
}

export interface MealPlanEntryDish {
  id: number;
  recipeId?: number;
  recipeName?: string;
  manualName?: string;
  manualQuantity?: number;
  manualUnit?: 'GRAM' | 'MILLILITER' | 'PIECE';
  sortOrder: number;
}

export interface MealPlanEntry {
  id: number;
  date: string;
  mealType: 'BREAKFAST' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER';
  dishes: MealPlanEntryDish[];
}

export interface ShoppingListItem {
  id: number;
  weekStartDate: string;
  ingredientName: string;
  totalQuantity: number;
  unit: 'GRAM' | 'MILLILITER' | 'PIECE';
  checked: boolean;
  manual: boolean;
}

export interface CreateRecipeRequest {
  name: string;
  description: string;
  photoUrl: string;
  instructions: string;
  ingredients: IngredientRequest[];
}

export interface IngredientRequest {
  name: string;
  quantity: number;
  unit: 'GRAM' | 'MILLILITER' | 'PIECE';
}

export interface CreateDishRequest {
  recipeId?: number;
  manualName?: string;
  manualQuantity?: number;
  manualUnit?: 'GRAM' | 'MILLILITER' | 'PIECE';
  sortOrder: number;
}

export interface CreateMealPlanEntryRequest {
  date: string;
  mealType: 'BREAKFAST' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER';
  dishes: CreateDishRequest[];
}

export interface CreateMealPlanEntriesRequest {
  entries: CreateMealPlanEntryRequest[];
}

export interface CreateManualItemRequest {
  ingredientName: string;
  totalQuantity: number;
  unit: 'GRAM' | 'MILLILITER' | 'PIECE';
}

export interface JoinCoupleRequest {
  inviteCode: string;
}
