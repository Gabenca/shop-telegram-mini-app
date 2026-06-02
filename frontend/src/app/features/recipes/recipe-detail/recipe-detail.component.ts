import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RecipeService } from '../../../shared/services/recipe.service';
import { MealPlanService } from '../../../shared/services/meal-plan.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { Recipe, MealPlanEntry } from '../../../shared/models';

@Component({
  selector: 'app-recipe-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './recipe-detail.component.html',
  styleUrl: './recipe-detail.component.scss'
})
export class RecipeDetailComponent implements OnInit, OnDestroy {
  recipe: Recipe | null = null;
  activeTab: 'ingredients' | 'instructions' = 'ingredients';
  showAddToPlanModal = false;

  selectedDays: boolean[] = [false, false, false, false, false, false, false];
  selectedMealType: 'BREAKFAST' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER' = 'BREAKFAST';

  dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
  mealTypes = [
    { value: 'BREAKFAST' as const, label: 'Завтрак' },
    { value: 'LUNCH' as const, label: 'Обед' },
    { value: 'AFTERNOON_SNACK' as const, label: 'Полдник' },
    { value: 'DINNER' as const, label: 'Ужин' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recipeService: RecipeService,
    private mealPlanService: MealPlanService,
    private telegramService: TelegramService
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.recipeService.getRecipe(id).subscribe({
      next: (recipe) => {
        this.recipe = recipe;
      }
    });

    this.telegramService.showBackButton(() => {
      this.router.navigate(['/recipes']);
    });
  }

  ngOnDestroy() {
    this.telegramService.hideBackButton();
  }

  setTab(tab: 'ingredients' | 'instructions') {
    this.activeTab = tab;
  }

  editRecipe() {
    if (this.recipe) {
      this.router.navigate(['/recipes', this.recipe.id, 'edit']);
    }
  }

  deleteRecipe() {
    if (this.recipe && confirm('Удалить рецепт?')) {
      this.recipeService.deleteRecipe(this.recipe.id).subscribe({
        next: () => {
          this.router.navigate(['/recipes']);
        }
      });
    }
  }

  openAddToPlanModal() {
    this.showAddToPlanModal = true;
    this.selectedDays = [false, false, false, false, false, false, false];
    this.selectedMealType = 'BREAKFAST';
  }

  closeAddToPlanModal() {
    this.showAddToPlanModal = false;
  }

  addToPlan() {
    if (!this.recipe) return;

    const today = new Date();
    const dayOfWeek = today.getDay();
    const monday = new Date(today);
    monday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));

    this.selectedDays.forEach((selected, index) => {
      if (selected) {
        const date = new Date(monday);
        date.setDate(monday.getDate() + index);
        const dateStr = date.toISOString().split('T')[0];

        this.mealPlanService.addMealPlanEntry({
          date: dateStr,
          recipeId: this.recipe!.id,
          mealType: this.selectedMealType
        }).subscribe();
      }
    });

    this.closeAddToPlanModal();
    this.telegramService.hapticFeedback('medium');
  }
}
