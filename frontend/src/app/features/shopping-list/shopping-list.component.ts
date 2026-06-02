import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ShoppingListService } from '../../shared/services/shopping-list.service';
import { TelegramService } from '../../core/services/telegram.service';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { ShoppingListItem } from '../../shared/models';

@Component({
  selector: 'app-shopping-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './shopping-list.component.html',
  styleUrl: './shopping-list.component.scss'
})
export class ShoppingListComponent implements OnInit, OnDestroy {
  weekStart: Date = new Date();
  items: ShoppingListItem[] = [];

  showAddModal = false;
  newItemName = '';
  newItemQuantity = 0;
  newItemUnit: 'GRAM' | 'MILLILITER' | 'PIECE' = 'GRAM';

  units = [
    { value: 'GRAM' as const, label: 'г' },
    { value: 'MILLILITER' as const, label: 'мл' },
    { value: 'PIECE' as const, label: 'шт' }
  ];

  constructor(
    private router: Router,
    private shoppingListService: ShoppingListService,
    private telegramService: TelegramService
  ) {}

  ngOnInit() {
    const dayOfWeek = this.weekStart.getDay();
    const monday = new Date(this.weekStart);
    monday.setDate(this.weekStart.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
    this.weekStart = monday;
    this.loadShoppingList();

    this.telegramService.showBackButton(() => {
      this.router.navigate(['/']);
    });
  }

  ngOnDestroy() {
    this.telegramService.hideBackButton();
  }

  loadShoppingList() {
    const weekStartStr = this.weekStart.toISOString().split('T')[0];
    this.shoppingListService.loadShoppingList(weekStartStr).subscribe({
      next: (items) => {
        this.items = items;
      }
    });
  }

  toggleItem(item: ShoppingListItem) {
    this.shoppingListService.toggleItemChecked(item.id).subscribe();
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

    const weekStartStr = this.weekStart.toISOString().split('T')[0];
    this.shoppingListService.addManualItem({
      ingredientName: this.newItemName,
      totalQuantity: this.newItemQuantity,
      unit: this.newItemUnit
    }, weekStartStr).subscribe({
      next: () => {
        this.closeAddModal();
        this.telegramService.hapticFeedback('medium');
      }
    });
  }

  regenerateList() {
    const weekStartStr = this.weekStart.toISOString().split('T')[0];
    this.shoppingListService.regenerateShoppingList(weekStartStr).subscribe();
  }

  getWeekRange(): string {
    const end = new Date(this.weekStart);
    end.setDate(this.weekStart.getDate() + 6);
    const startStr = this.weekStart.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    const endStr = end.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    return `${startStr} — ${endStr}`;
  }
}
