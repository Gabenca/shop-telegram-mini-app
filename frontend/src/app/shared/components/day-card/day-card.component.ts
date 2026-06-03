import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MealPlanEntry, MealPlanEntryDish } from '../../models';

@Component({
  selector: 'app-day-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './day-card.component.html',
  styleUrl: './day-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DayCardComponent {
  @Input({ required: true }) date!: string;
  @Input({ required: true }) dayName!: string;
  @Input({ required: true }) entries: MealPlanEntry[] = [];
  @Output() slotClick = new EventEmitter<{ date: string; mealType: string }>();
  @Output() deleteEntry = new EventEmitter<number>();

  mealTypes = [
    { type: 'BREAKFAST', label: 'Завтрак', icon: 'breakfast' },
    { type: 'LUNCH', label: 'Обед', icon: 'lunch' },
    { type: 'AFTERNOON_SNACK', label: 'Перекус', icon: 'snack' },
    { type: 'DINNER', label: 'Ужин', icon: 'dinner' }
  ];

  getEntry(mealType: string): MealPlanEntry | undefined {
    return this.entries.find(e => e.mealType === mealType);
  }

  getDishes(entry: MealPlanEntry | undefined): MealPlanEntryDish[] {
    if (!entry?.dishes) return [];
    return [...entry.dishes].sort((a, b) => a.sortOrder - b.sortOrder);
  }

  getDishDisplayName(dish: MealPlanEntryDish): string {
    return dish.recipeName ?? dish.manualName ?? '—';
  }

  isManual(dish: MealPlanEntryDish): boolean {
    return !!dish.manualName;
  }

  onSlotClick(mealType: string) {
    this.slotClick.emit({ date: this.date, mealType });
  }

  onDeleteEntry(entryId: number | undefined, event: Event) {
    event.stopPropagation();
    if (entryId !== undefined) {
      this.deleteEntry.emit(entryId);
    }
  }
}