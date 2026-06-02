import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { ShoppingListItem, CreateManualItemRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService {

  private shoppingList$ = new BehaviorSubject<ShoppingListItem[]>([]);

  constructor(private api: ApiService) {}

  getShoppingListObservable(): Observable<ShoppingListItem[]> {
    return this.shoppingList$.asObservable();
  }

  loadShoppingList(weekStart: string): Observable<ShoppingListItem[]> {
    return this.api.http.get<ShoppingListItem[]>(`${this.api.baseUrl}/shopping-list`, {
      params: { weekStart }
    }).pipe(
      tap(items => this.shoppingList$.next(items))
    );
  }

  regenerateShoppingList(weekStart: string): Observable<ShoppingListItem[]> {
    return this.api.http.post<ShoppingListItem[]>(`${this.api.baseUrl}/shopping-list/regenerate`, {}, {
      params: { weekStart }
    }).pipe(
      tap(items => this.shoppingList$.next(items))
    );
  }

  addManualItem(request: CreateManualItemRequest, weekStart: string): Observable<ShoppingListItem> {
    return this.api.http.post<ShoppingListItem>(`${this.api.baseUrl}/shopping-list/items`, request, {
      params: { weekStart }
    }).pipe(
      tap(item => {
        const current = this.shoppingList$.value;
        this.shoppingList$.next([...current, item]);
      })
    );
  }

  toggleItemChecked(id: number): Observable<ShoppingListItem> {
    return this.api.http.patch<ShoppingListItem>(`${this.api.baseUrl}/shopping-list/items/${id}`, {}).pipe(
      tap(updatedItem => {
        const current = this.shoppingList$.value;
        const index = current.findIndex(i => i.id === id);
        if (index !== -1) {
          current[index] = updatedItem;
          this.shoppingList$.next([...current]);
        }
      })
    );
  }

  deleteItem(id: number): Observable<void> {
    return this.api.http.delete<void>(`${this.api.baseUrl}/shopping-list/items/${id}`).pipe(
      tap(() => {
        const current = this.shoppingList$.value;
        this.shoppingList$.next(current.filter(i => i.id !== id));
      })
    );
  }
}
