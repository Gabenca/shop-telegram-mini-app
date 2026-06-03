import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { MealPlanEntry, CreateMealPlanEntryRequest, CreateMealPlanEntriesRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class MealPlanService {

  private mealPlan$ = new BehaviorSubject<MealPlanEntry[]>([]);

  constructor(private api: ApiService) {}

  getMealPlanObservable(): Observable<MealPlanEntry[]> {
    return this.mealPlan$.asObservable();
  }

  loadMealPlan(weekStart: string): Observable<MealPlanEntry[]> {
    return this.api.http.get<MealPlanEntry[]>(`${this.api.baseUrl}/meal-plan`, {
      params: { weekStart }
    }).pipe(
      tap(entries => this.mealPlan$.next(entries))
    );
  }

  addMealPlanEntry(request: CreateMealPlanEntryRequest): Observable<MealPlanEntry> {
    return this.api.http.post<MealPlanEntry>(`${this.api.baseUrl}/meal-plan`, request).pipe(
      tap(entry => {
        const current = this.mealPlan$.value;
        this.mealPlan$.next([...current, entry]);
      })
    );
  }

  addMealPlanEntries(request: CreateMealPlanEntriesRequest): Observable<MealPlanEntry[]> {
    return this.api.http.post<MealPlanEntry[]>(`${this.api.baseUrl}/meal-plan/batch`, request).pipe(
      tap(entries => {
        const current = this.mealPlan$.value;
        this.mealPlan$.next([...current, ...entries]);
      })
    );
  }

  updateMealPlanEntry(id: number, request: CreateMealPlanEntryRequest): Observable<MealPlanEntry> {
    return this.api.http.put<MealPlanEntry>(`${this.api.baseUrl}/meal-plan/${id}`, request).pipe(
      tap(updated => {
        const current = this.mealPlan$.value;
        this.mealPlan$.next(current.map(e => e.id === id ? updated : e));
      })
    );
  }

  deleteMealPlanEntry(id: number): Observable<void> {
    return this.api.http.delete<void>(`${this.api.baseUrl}/meal-plan/${id}`).pipe(
      tap(() => {
        const current = this.mealPlan$.value;
        this.mealPlan$.next(current.filter(e => e.id !== id));
      })
    );
  }

  deleteDish(dishId: number): Observable<void> {
    return this.api.http.delete<void>(`${this.api.baseUrl}/meal-plan/dishes/${dishId}`).pipe(
      tap(() => {
        const current = this.mealPlan$.value;
        this.mealPlan$.next(
          current
            .map(e => ({ ...e, dishes: e.dishes.filter(d => d.id !== dishId) }))
            .filter(e => e.dishes.length > 0)
        );
      })
    );
  }
}