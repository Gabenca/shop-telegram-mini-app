import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MealPlanService } from '../../shared/services/meal-plan.service';
import { RecipeService } from '../../shared/services/recipe.service';
import { ShoppingListService } from '../../shared/services/shopping-list.service';
import { TelegramService } from '../../core/services/telegram.service';
import { DayCardComponent } from '../../shared/components/day-card/day-card.component';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { MealPlanEntry, Recipe } from '../../shared/models';

@Component({
  selector: 'app-meal-plan',
  standalone: true,
  imports: [CommonModule, DayCardComponent, ModalComponent],
  templateUrl: './meal-plan.component.html',
  styleUrl: './meal-plan.component.scss'
})
export class MealPlanComponent implements OnInit, OnDestroy {
  weekStart: Date = new Date();
  days: { date: string; dayName: string; entries: MealPlanEntry[] }[] = [];
  recipes: Recipe[] = [];

  showRecipeModal = false;
  selectedDate = '';
  selectedMealType: 'BREAKFAST' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER' = 'BREAKFAST';

  dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

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
    this.recipeService.loadRecipes().subscribe({
      next: (recipes) => {
        this.recipes = recipes;
      }
    });
  }

  setWeekStart(date: Date) {
    const dayOfWeek = date.getDay();
    const monday = new Date(date);
    monday.setDate(date.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
    this.weekStart = monday;
    this.loadWeek();
  }

  loadWeek() {
    const weekStartStr = this.weekStart.toISOString().split('T')[0];
    this.mealPlanService.loadMealPlan(weekStartStr).subscribe({
      next: (entries) => {
        this.days = [];
        for (let i = 0; i < 7; i++) {
          const date = new Date(this.weekStart);
          date.setDate(this.weekStart.getDate() + i);
          const dateStr = date.toISOString().split('T')[0];
          const dayEntries = entries.filter(e => e.date === dateStr);
          this.days.push({
            date: dateStr,
            dayName: this.dayNames[i],
            entries: dayEntries
          });
        }
      }
    });
  }

  previousWeek() {
    const prevWeek = new Date(this.weekStart);
    prevWeek.setDate(this.weekStart.getDate() - 7);
    this.setWeekStart(prevWeek);
  }

  nextWeek() {
    const nextWeek = new Date(this.weekStart);
    nextWeek.setDate(this.weekStart.getDate() + 7);
    this.setWeekStart(nextWeek);
  }

  onSlotClick(event: { date: string; mealType: string }) {
    const existingEntry = this.days
      .find(d => d.date === event.date)?.entries
      .find(e => e.mealType === event.mealType);

    if (existingEntry) {
      if (confirm('Удалить из плана?')) {
        this.mealPlanService.deleteMealPlanEntry(existingEntry.id).subscribe({
          next: () => {
            this.loadWeek();
          }
        });
      }
    } else {
      this.selectedDate = event.date;
      this.selectedMealType = event.mealType as any;
      this.showRecipeModal = true;
    }
  }

  closeRecipeModal() {
    this.showRecipeModal = false;
  }

  selectRecipe(recipe: Recipe) {
    this.mealPlanService.addMealPlanEntry({
      date: this.selectedDate,
      recipeId: recipe.id,
      mealType: this.selectedMealType
    }).subscribe({
      next: () => {
        this.closeRecipeModal();
        this.loadWeek();
        this.telegramService.hapticFeedback('light');
      }
    });
  }

  generateShoppingList() {
    const weekStartStr = this.weekStart.toISOString().split('T')[0];
    this.shoppingListService.regenerateShoppingList(weekStartStr).subscribe({
      next: () => {
        this.router.navigate(['/shopping']);
      }
    });
  }

  getWeekRange(): string {
    const end = new Date(this.weekStart);
    end.setDate(this.weekStart.getDate() + 6);
    const startStr = this.weekStart.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    const endStr = end.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    return `${startStr} — ${endStr}`;
  }
}
