import { Component, OnInit, OnDestroy, inject, DestroyRef, signal, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RecipeService } from '../../../shared/services/recipe.service';
import { MealPlanService } from '../../../shared/services/meal-plan.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { HomeButtonComponent } from '../../../shared/components/home-button/home-button.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { Recipe } from '../../../shared/models';
import { formatDate, getMonday } from '../../../shared/utils/date.utils';
import { UnitPipe } from '../../../shared/pipes/unit.pipe';

@Component({
  selector: 'app-recipe-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, HomeButtonComponent, ModalComponent, UnitPipe],
  templateUrl: './recipe-detail.component.html',
  styleUrl: './recipe-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RecipeDetailComponent implements OnInit, OnDestroy {
  recipe = signal<Recipe | null>(null);
  isLoading = signal(true);
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

  private destroyRef = inject(DestroyRef);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recipeService: RecipeService,
    private mealPlanService: MealPlanService,
    private telegramService: TelegramService
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.recipeService.getRecipe(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (recipe) => {
        this.recipe.set(recipe);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load recipe', err);
        this.isLoading.set(false);
        this.telegramService.showPopup('Ошибка', 'Не удалось загрузить рецепт');
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
    const r = this.recipe();
    if (r) {
      this.router.navigate(['/recipes', r.id, 'edit']);
    }
  }

  async deleteRecipe() {
    const r = this.recipe();
    if (!r) return;
    const confirmed = await this.telegramService.showConfirm('Подтверждение', 'Удалить рецепт?');
    if (confirmed) {
      this.recipeService.deleteRecipe(r.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.router.navigate(['/recipes']);
        },
        error: (err) => {
          console.error('Failed to delete recipe', err);
          this.telegramService.showPopup('Ошибка', 'Не удалось удалить рецепт');
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
    const r = this.recipe();
    if (!r) return;

    const today = new Date();
    const monday = getMonday(today);

    this.selectedDays.forEach((selected, index) => {
      if (selected) {
        const date = new Date(monday);
        date.setDate(monday.getDate() + index);
        const dateStr = formatDate(date);

        this.mealPlanService.addMealPlanEntry({
          date: dateStr,
          mealType: this.selectedMealType,
          dishes: [{ recipeId: r.id, sortOrder: 0 }]
        }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
          error: (err) => {
            console.error('Failed to add meal plan entry', err);
          }
        });
      }
    });

    this.closeAddToPlanModal();
    this.telegramService.hapticFeedback('medium');
  }
}
