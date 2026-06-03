import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, EMPTY, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
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
    const current = this.shoppingList$.value;
    const index = current.findIndex(i => i.id === id);
    if (index === -1) {
      return EMPTY;
    }
    const previousState = current[index].checked;
    const newChecked = !previousState;

    const optimistic = current.slice();
    optimistic[index] = { ...optimistic[index], checked: newChecked };
    this.shoppingList$.next(optimistic);

    return this.api.http.patch<ShoppingListItem>(
      `${this.api.baseUrl}/shopping-list/items/${id}`,
      { checked: newChecked }
    ).pipe(
      tap(updatedItem => {
        const cur = this.shoppingList$.value;
        const idx = cur.findIndex(i => i.id === id);
        if (idx !== -1) {
          cur[idx] = updatedItem;
          this.shoppingList$.next([...cur]);
        }
      }),
      catchError(err => {
        const cur = this.shoppingList$.value;
        const idx = cur.findIndex(i => i.id === id);
        if (idx !== -1) {
          cur[idx] = { ...cur[idx], checked: previousState };
          this.shoppingList$.next([...cur]);
        }
        return throwError(() => err);
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
