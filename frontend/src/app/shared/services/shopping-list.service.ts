import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { ShoppingListItem, CreateManualItemRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService extends ApiService {

  private shoppingList$ = new BehaviorSubject<ShoppingListItem[]>([]);

  getShoppingListObservable(): Observable<ShoppingListItem[]> {
    return this.shoppingList$.asObservable();
  }

  loadShoppingList(weekStart: string): Observable<ShoppingListItem[]> {
    return this.http.get<ShoppingListItem[]>(`${this.baseUrl}/shopping-list`, {
      params: { weekStart }
    }).pipe(
      tap(items => this.shoppingList$.next(items))
    );
  }

  regenerateShoppingList(weekStart: string): Observable<ShoppingListItem[]> {
    return this.http.post<ShoppingListItem[]>(`${this.baseUrl}/shopping-list/regenerate`, {}, {
      params: { weekStart }
    }).pipe(
      tap(items => this.shoppingList$.next(items))
    );
  }

  addManualItem(request: CreateManualItemRequest, weekStart: string): Observable<ShoppingListItem> {
    return this.http.post<ShoppingListItem>(`${this.baseUrl}/shopping-list/items`, request, {
      params: { weekStart }
    }).pipe(
      tap(item => {
        const current = this.shoppingList$.value;
        this.shoppingList$.next([...current, item]);
      })
    );
  }

  toggleItemChecked(id: number): Observable<ShoppingListItem> {
    return this.http.patch<ShoppingListItem>(`${this.baseUrl}/shopping-list/items/${id}`, {}).pipe(
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
    return this.http.delete<void>(`${this.baseUrl}/shopping-list/items/${id}`).pipe(
      tap(() => {
        const current = this.shoppingList$.value;
        this.shoppingList$.next(current.filter(i => i.id !== id));
      })
    );
  }
}
