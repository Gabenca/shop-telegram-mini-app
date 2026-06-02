import { Routes } from '@angular/router';
import { coupleGuard } from './core/guards/couple.guard';

export const routes: Routes = [
  {
    path: 'join',
    loadComponent: () => import('./features/couple/couple-join/couple-join.component')
      .then(m => m.CoupleJoinComponent)
  },
  {
    path: '',
    loadComponent: () => import('./features/home/home.component')
      .then(m => m.HomeComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'recipes',
    loadComponent: () => import('./features/recipes/recipe-list/recipe-list.component')
      .then(m => m.RecipeListComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'recipes/new',
    loadComponent: () => import('./features/recipes/recipe-form/recipe-form.component')
      .then(m => m.RecipeFormComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'recipes/:id',
    loadComponent: () => import('./features/recipes/recipe-detail/recipe-detail.component')
      .then(m => m.RecipeDetailComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'recipes/:id/edit',
    loadComponent: () => import('./features/recipes/recipe-form/recipe-form.component')
      .then(m => m.RecipeFormComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'plan',
    loadComponent: () => import('./features/meal-plan/meal-plan.component')
      .then(m => m.MealPlanComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'shopping',
    loadComponent: () => import('./features/shopping-list/shopping-list.component')
      .then(m => m.ShoppingListComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'couple',
    loadComponent: () => import('./features/couple/couple-profile/couple-profile.component')
      .then(m => m.CoupleProfileComponent),
    canActivate: [coupleGuard]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
