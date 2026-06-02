import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MealPlanEntry } from '../../models';

@Component({
  selector: 'app-day-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './day-card.component.html',
  styleUrl: './day-card.component.scss'
})
export class DayCardComponent {
  @Input() date!: string;
  @Input() dayName!: string;
  @Input() entries: MealPlanEntry[] = [];
  @Output() slotClick = new EventEmitter<{ date: string; mealType: string }>();

  mealTypes = [
    { type: 'BREAKFAST', label: 'Завтрак', icon: '🌅' },
    { type: 'LUNCH', label: 'Обед', icon: '☀️' },
    { type: 'AFTERNOON_SNACK', label: 'Полдник', icon: '🍪' },
    { type: 'DINNER', label: 'Ужин', icon: '🌙' }
  ];

  getRecipeForMeal(mealType: string): string {
    const entry = this.entries.find(e => e.mealType === mealType);
    return entry ? entry.recipe.name : '—';
  }

  onSlotClick(mealType: string) {
    this.slotClick.emit({ date: this.date, mealType });
  }
}
