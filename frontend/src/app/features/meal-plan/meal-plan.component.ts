import { Component, OnInit, OnDestroy, inject, DestroyRef, signal, ChangeDetectionStrategy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MealPlanService } from '../../shared/services/meal-plan.service';
import { RecipeService } from '../../shared/services/recipe.service';
import { ShoppingListService } from '../../shared/services/shopping-list.service';
import { TelegramService } from '../../core/services/telegram.service';
import { HomeButtonComponent } from '../../shared/components/home-button/home-button.component';
import { DayCardComponent } from '../../shared/components/day-card/day-card.component';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { MealPlanEntry, MealPlanEntryDish, Recipe, CreateMealPlanEntryRequest, CreateDishRequest } from '../../shared/models';
import { formatDate, getMonday, formatWeekRange } from '../../shared/utils/date.utils';

interface DishDraft {
  recipeId?: number;
  recipeName?: string;
  manualName?: string;
  manualQuantity?: number;
  manualUnit?: 'GRAM' | 'MILLILITER' | 'PIECE';
}

@Component({
  selector: 'app-meal-plan',
  standalone: true,
  imports: [CommonModule, FormsModule, HomeButtonComponent, DayCardComponent, ModalComponent],
  templateUrl: './meal-plan.component.html',
  styleUrl: './meal-plan.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MealPlanComponent implements OnInit, OnDestroy {
  weekStart = signal<Date>(new Date());
  days: { date: string; dayName: string; entries: MealPlanEntry[] }[] = [];
  recipes: Recipe[] = [];
  isLoading = signal(false);
  weekRange = computed(() => formatWeekRange(this.weekStart()));
  isEmptyWeek = computed(() => this.days.every(d => d.entries.length === 0));

  showRecipeModal = false;
  modalStep: 'recipe' | 'slots' = 'recipe';
  selectedRecipe: Recipe | null = null;
  editingEntry: MealPlanEntry | null = null;
  modalDishes: DishDraft[] = [];
  modalDays: boolean[] = [false, false, false, false, false, false, false];
  modalMealTypes: Record<string, boolean> = {
    BREAKFAST: false,
    LUNCH: false,
    AFTERNOON_SNACK: false,
    DINNER: false
  };
  isManualMode = false;
  newManualName = '';
  newManualQuantity = 0;
  newManualUnit: 'GRAM' | 'MILLILITER' | 'PIECE' = 'GRAM';

  dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

  mealTypes = [
    { type: 'BREAKFAST', label: 'Завтрак', icon: '🌅' },
    { type: 'LUNCH', label: 'Обед', icon: '☀️' },
    { type: 'AFTERNOON_SNACK', label: 'Полдник', icon: '🍪' },
    { type: 'DINNER', label: 'Ужин', icon: '🌙' }
  ];

  units = [
    { value: 'GRAM' as const, label: 'г' },
    { value: 'MILLILITER' as const, label: 'мл' },
    { value: 'PIECE' as const, label: 'шт' }
  ];

  private destroyRef = inject(DestroyRef);

  constructor(
    private router: Router,
    private mealPlanService: MealPlanService,
    private recipeService: RecipeService,
    private shoppingListService: ShoppingListService,
    private telegramService: TelegramService
  ) {}

  ngOnInit() {
    this.setWeekStart(new Date());
    this.loadRecipes();

    this.telegramService.showBackButton(() => {
      this.router.navigate(['/']);
    });
  }

  ngOnDestroy() {
    this.telegramService.hideBackButton();
  }

  loadRecipes() {
    this.recipeService.loadRecipes().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (recipes) => {
        this.recipes = recipes;
      },
      error: (err) => {
        console.error('Failed to load recipes', err);
      }
    });
  }

  setWeekStart(date: Date) {
    this.weekStart.set(getMonday(date));
    this.loadWeek();
  }

  loadWeek() {
    this.isLoading.set(true);
    const weekStartStr = formatDate(this.weekStart());
    this.mealPlanService.loadMealPlan(weekStartStr).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (entries) => {
        this.days = [];
        for (let i = 0; i < 7; i++) {
          const date = new Date(this.weekStart());
          date.setDate(this.weekStart().getDate() + i);
          const dateStr = formatDate(date);
          const dayEntries = entries.filter(e => e.date === dateStr);
          this.days.push({
            date: dateStr,
            dayName: this.dayNames[i],
            entries: dayEntries
          });
        }
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load meal plan', err);
        this.isLoading.set(false);
      }
    });
  }

  previousWeek() {
    this.telegramService.hapticFeedback('light');
    const prevWeek = new Date(this.weekStart());
    prevWeek.setDate(this.weekStart().getDate() - 7);
    this.setWeekStart(prevWeek);
  }

  nextWeek() {
    this.telegramService.hapticFeedback('light');
    const nextWeek = new Date(this.weekStart());
    nextWeek.setDate(this.weekStart().getDate() + 7);
    this.setWeekStart(nextWeek);
  }

  async onSlotClick(event: { date: string; mealType: string }) {
    const existingEntry = this.days
      .find(d => d.date === event.date)?.entries
      .find(e => e.mealType === event.mealType);

    if (existingEntry) {
      this.openEditEntryModal(existingEntry);
    } else {
      this.openAddEntryModal(event.date, event.mealType);
    }
  }

  openAddEntryModal(date: string, mealType: string) {
    this.telegramService.hapticFeedback('light');
    this.editingEntry = null;
    this.modalDishes = [];
    this.modalDays = [false, false, false, false, false, false, false];
    this.modalMealTypes = {
      BREAKFAST: false,
      LUNCH: false,
      AFTERNOON_SNACK: false,
      DINNER: false
    };
    this.modalStep = 'recipe';
    this.selectedRecipe = null;
    this.isManualMode = false;
    this.newManualName = '';
    this.newManualQuantity = 0;
    this.newManualUnit = 'GRAM';

    const dayIndex = this.days.findIndex(d => d.date === date);
    if (dayIndex !== -1) {
      this.modalDays[dayIndex] = true;
    }
    this.modalMealTypes[mealType] = true;
    this.showRecipeModal = true;
  }

  openEditEntryModal(entry: MealPlanEntry) {
    this.telegramService.hapticFeedback('light');
    this.openEditEntryModalInternal(entry);
  }

  private openEditEntryModalInternal(entry: MealPlanEntry) {
    this.editingEntry = entry;
    this.modalDishes = entry.dishes.map(d => ({
      recipeId: d.recipeId,
      recipeName: d.recipeName,
      manualName: d.manualName,
      manualQuantity: d.manualQuantity,
      manualUnit: d.manualUnit
    }));
    this.modalStep = 'recipe';
    this.selectedRecipe = null;
    this.isManualMode = false;
    this.showRecipeModal = true;
  }

  closeRecipeModal() {
    this.telegramService.hapticFeedback('light');
    this.showRecipeModal = false;
    this.modalStep = 'recipe';
    this.selectedRecipe = null;
    this.editingEntry = null;
    this.modalDishes = [];
    this.isManualMode = false;
  }

  selectRecipe(recipe: Recipe) {
    this.telegramService.hapticFeedback('light');
    this.selectedRecipe = recipe;
  }

  backToRecipeStep() {
    this.telegramService.hapticFeedback('light');
    this.modalStep = 'recipe';
    this.selectedRecipe = null;
  }

  toggleManualMode() {
    this.telegramService.hapticFeedback('light');
    this.isManualMode = !this.isManualMode;
  }

  addCurrentRecipeToDishes() {
    if (!this.selectedRecipe) return;
    this.telegramService.hapticFeedback('light');
    this.modalDishes.push({
      recipeId: this.selectedRecipe.id,
      recipeName: this.selectedRecipe.name
    });
    this.selectedRecipe = null;
  }

  addManualDish() {
    const name = this.newManualName.trim();
    if (!name) return;
    this.telegramService.hapticFeedback('light');
    this.modalDishes.push({
      manualName: name,
      manualQuantity: this.newManualQuantity,
      manualUnit: this.newManualUnit
    });
    this.newManualName = '';
    this.newManualQuantity = 0;
    this.newManualUnit = 'GRAM';
    this.isManualMode = false;
  }

  removeDishFromModal(index: number) {
    this.telegramService.hapticFeedback('light');
    this.modalDishes.splice(index, 1);
  }

  async onDeleteEntry(entryId: number) {
    const confirmed = await this.telegramService.showConfirm('Удалить', 'Удалить все блюда этого приёма пищи?');
    if (!confirmed) return;

    this.mealPlanService.deleteMealPlanEntry(entryId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.loadWeek();
        this.telegramService.hapticFeedback('medium');
      },
      error: (err) => {
        console.error('Failed to delete entry', err);
      }
    });
  }

  saveEntry() {
    if (this.editingEntry) {
      if (this.modalDishes.length === 0) {
        this.onDeleteEntry(this.editingEntry.id);
        this.closeRecipeModal();
        return;
      }

      const request: CreateMealPlanEntryRequest = {
        date: this.editingEntry.date,
        mealType: this.editingEntry.mealType,
        dishes: this.modalDishes.map((d, idx) => this.dishDraftToRequest(d, idx))
      };

      this.mealPlanService.updateMealPlanEntry(this.editingEntry.id, request)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.closeRecipeModal();
            this.loadWeek();
            this.telegramService.hapticFeedback('medium');
          },
          error: (err) => {
            console.error('Failed to update entry', err);
          }
        });
    } else {
      this.addNewEntriesBatch();
    }
  }

  addNewEntriesBatch() {
    if (this.modalDishes.length === 0) return;

    const requests: CreateMealPlanEntryRequest[] = [];
    this.modalDays.forEach((daySelected, dayIndex) => {
      if (!daySelected) return;
      const date = new Date(this.weekStart());
      date.setDate(this.weekStart().getDate() + dayIndex);
      const dateStr = formatDate(date);

      Object.keys(this.modalMealTypes).forEach(mealType => {
        if (!this.modalMealTypes[mealType]) return;
        requests.push({
          date: dateStr,
          mealType: mealType as any,
          dishes: this.modalDishes.map((d, idx) => this.dishDraftToRequest(d, idx))
        });
      });
    });

    if (requests.length === 0) return;

    this.mealPlanService.addMealPlanEntries({ entries: requests })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.closeRecipeModal();
          this.loadWeek();
          this.telegramService.hapticFeedback('medium');
        },
        error: (err) => {
          console.error('Failed to add meal plan entries', err);
        }
      });
  }

  dishDraftToRequest(draft: DishDraft, index: number): CreateDishRequest {
    return {
      recipeId: draft.recipeId,
      manualName: draft.manualName,
      manualQuantity: draft.manualQuantity,
      manualUnit: draft.manualUnit,
      sortOrder: index
    };
  }

  getDishDisplayName(dish: DishDraft): string {
    if (dish.recipeName) return dish.recipeName;
    if (dish.manualName) {
      let text = dish.manualName;
      if (dish.manualQuantity) {
        text += ` (${dish.manualQuantity} ${this.unitLabel(dish.manualUnit)})`;
      }
      return text;
    }
    return '—';
  }

  unitLabel(unit: 'GRAM' | 'MILLILITER' | 'PIECE' | undefined): string {
    return this.units.find(u => u.value === unit)?.label ?? '';
  }

  generateShoppingList() {
    this.telegramService.hapticFeedback('medium');
    const weekStartStr = formatDate(this.weekStart());
    this.shoppingListService.regenerateShoppingList(weekStartStr).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.router.navigate(['/shopping']);
      },
      error: (err) => {
        console.error('Failed to generate shopping list', err);
      }
    });
  }
}