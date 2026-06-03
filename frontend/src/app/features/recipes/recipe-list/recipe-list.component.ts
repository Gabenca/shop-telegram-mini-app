import { Component, OnInit, inject, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RecipeService } from '../../../shared/services/recipe.service';
import { RecipeCardComponent } from '../../../shared/components/recipe-card/recipe-card.component';
import { HomeButtonComponent } from '../../../shared/components/home-button/home-button.component';
import { Recipe } from '../../../shared/models';

@Component({
  selector: 'app-recipe-list',
  standalone: true,
  imports: [CommonModule, RecipeCardComponent, HomeButtonComponent],
  templateUrl: './recipe-list.component.html',
  styleUrl: './recipe-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RecipeListComponent implements OnInit {
  recipes: Recipe[] = [];
  isLoading = signal(false);
  private destroyRef = inject(DestroyRef);

  constructor(
    private recipeService: RecipeService,
    private router: Router
  ) {}

  ngOnInit() {
    this.isLoading.set(true);
    this.recipeService.loadRecipes().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (recipes) => {
        this.recipes = recipes;
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load recipes', err);
        this.isLoading.set(false);
      }
    });
  }

  addRecipe() {
    this.router.navigate(['/recipes/new']);
  }

  navigateToRecipe(recipe: Recipe) {
    this.router.navigate(['/recipes', recipe.id]);
  }
}
