import { Component, OnInit, OnDestroy, inject, DestroyRef, signal, ChangeDetectionStrategy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ShoppingListService } from '../../shared/services/shopping-list.service';
import { TelegramService } from '../../core/services/telegram.service';
import { HomeButtonComponent } from '../../shared/components/home-button/home-button.component';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { ShoppingListItem } from '../../shared/models';
import { formatDate, getMonday, formatWeekRange } from '../../shared/utils/date.utils';
import { UnitPipe } from '../../shared/pipes/unit.pipe';

@Component({
  selector: 'app-shopping-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HomeButtonComponent, ModalComponent, UnitPipe],
  templateUrl: './shopping-list.component.html',
  styleUrl: './shopping-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShoppingListComponent implements OnInit, OnDestroy {
  weekStart = signal<Date>(new Date());
  items: ShoppingListItem[] = [];
  isLoading = signal(false);
  weekRange = computed(() => formatWeekRange(this.weekStart()));

  showAddModal = false;
  newItemName = '';
  newItemQuantity = 0;
  newItemUnit: 'GRAM' | 'MILLILITER' | 'PIECE' = 'GRAM';

  units = [
    { value: 'GRAM' as const, label: 'г' },
    { value: 'MILLILITER' as const, label: 'мл' },
    { value: 'PIECE' as const, label: 'шт' }
  ];

  private destroyRef = inject(DestroyRef);

  constructor(
    private router: Router,
    private shoppingListService: ShoppingListService,
    private telegramService: TelegramService
  ) {}

  ngOnInit() {
    this.weekStart.set(getMonday(this.weekStart()));
    this.loadShoppingList();

    this.shoppingListService.getShoppingListObservable().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (items) => {
        this.items = items;
      }
    });

    this.telegramService.showBackButton(() => {
      this.router.navigate(['/']);
    });
  }

  ngOnDestroy() {
    this.telegramService.hideBackButton();
  }

  loadShoppingList() {
    this.isLoading.set(true);
    const weekStartStr = formatDate(this.weekStart());
    this.shoppingListService.loadShoppingList(weekStartStr).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load shopping list', err);
        this.isLoading.set(false);
      }
    });
  }

  previousWeek() {
    const prevWeek = new Date(this.weekStart());
    prevWeek.setDate(this.weekStart().getDate() - 7);
    this.weekStart.set(getMonday(prevWeek));
    this.loadShoppingList();
  }

  nextWeek() {
    const nextWeek = new Date(this.weekStart());
    nextWeek.setDate(this.weekStart().getDate() + 7);
    this.weekStart.set(getMonday(nextWeek));
    this.loadShoppingList();
  }

  toggleItem(item: ShoppingListItem) {
    this.shoppingListService.toggleItemChecked(item.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      error: (err) => {
        console.error('Failed to toggle item', err);
      }
    });
    this.telegramService.hapticFeedback('light');
  }

  openAddModal() {
    this.showAddModal = true;
    this.newItemName = '';
    this.newItemQuantity = 0;
    this.newItemUnit = 'GRAM';
  }

  closeAddModal() {
    this.showAddModal = false;
  }

  addItem() {
    if (!this.newItemName.trim()) return;

    const weekStartStr = formatDate(this.weekStart());
    this.shoppingListService.addManualItem({
      ingredientName: this.newItemName,
      totalQuantity: this.newItemQuantity,
      unit: this.newItemUnit
    }, weekStartStr).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      error: (err) => {
        console.error('Failed to add item', err);
      }
    });
    this.closeAddModal();
    this.telegramService.hapticFeedback('medium');
  }

  regenerateList() {
    this.isLoading.set(true);
    const weekStartStr = formatDate(this.weekStart());
    this.shoppingListService.regenerateShoppingList(weekStartStr).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to regenerate shopping list', err);
        this.isLoading.set(false);
      }
    });
  }
}
