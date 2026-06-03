import { Routes } from '@angular/router';
import { coupleGuard } from './core/guards/couple.guard';

export const routes: Routes = [
  {
    path: 'join',
    loadComponent: () => import('./features/couple/couple-join/couple-join.component')
      .then(m => m.CoupleJoinComponent),
    data: { animation: 'JoinPage' }
  },
  {
    path: '',
    loadComponent: () => import('./features/home/home.component')
      .then(m => m.HomeComponent),
    canActivate: [coupleGuard],
    data: { animation: 'HomePage' }
  },
  {
    path: 'recipes',
    loadComponent: () => import('./features/recipes/recipe-list/recipe-list.component')
      .then(m => m.RecipeListComponent),
    canActivate: [coupleGuard],
    data: { animation: 'RecipesPage' }
  },
  {
    path: 'recipes/new',
    loadComponent: () => import('./features/recipes/recipe-form/recipe-form.component')
      .then(m => m.RecipeFormComponent),
    canActivate: [coupleGuard],
    data: { animation: 'RecipeFormPage' }
  },
  {
    path: 'recipes/:id',
    loadComponent: () => import('./features/recipes/recipe-detail/recipe-detail.component')
      .then(m => m.RecipeDetailComponent),
    canActivate: [coupleGuard],
    data: { animation: 'RecipeDetailPage' }
  },
  {
    path: 'recipes/:id/edit',
    loadComponent: () => import('./features/recipes/recipe-form/recipe-form.component')
      .then(m => m.RecipeFormComponent),
    canActivate: [coupleGuard],
    data: { animation: 'RecipeFormPage' }
  },
  {
    path: 'plan',
    loadComponent: () => import('./features/meal-plan/meal-plan.component')
      .then(m => m.MealPlanComponent),
    canActivate: [coupleGuard],
    data: { animation: 'PlanPage' }
  },
  {
    path: 'shopping',
    loadComponent: () => import('./features/shopping-list/shopping-list.component')
      .then(m => m.ShoppingListComponent),
    canActivate: [coupleGuard],
    data: { animation: 'ShoppingPage' }
  },
  {
    path: 'couple',
    loadComponent: () => import('./features/couple/couple-profile/couple-profile.component')
      .then(m => m.CoupleProfileComponent),
    canActivate: [coupleGuard],
    data: { animation: 'CouplePage' }
  },
  {
    path: '**',
    redirectTo: ''
  }
];
