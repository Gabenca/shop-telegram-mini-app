import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { MealPlanEntry, CreateMealPlanEntryRequest } from '../models';

import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { MealPlanEntry, CreateMealPlanEntryRequest } from '../models';

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

  deleteMealPlanEntry(id: number): Observable<void> {
    return this.api.http.delete<void>(`${this.api.baseUrl}/meal-plan/${id}`).pipe(
      tap(() => {
        const current = this.mealPlan$.value;
        this.mealPlan$.next(current.filter(e => e.id !== id));
      })
    );
  }
}
