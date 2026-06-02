import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { MealPlanEntry, CreateMealPlanEntryRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class MealPlanService extends ApiService {

  private mealPlan$ = new BehaviorSubject<MealPlanEntry[]>([]);

  getMealPlanObservable(): Observable<MealPlanEntry[]> {
    return this.mealPlan$.asObservable();
  }

  loadMealPlan(weekStart: string): Observable<MealPlanEntry[]> {
    return this.http.get<MealPlanEntry[]>(`${this.baseUrl}/meal-plan`, {
      params: { weekStart }
    }).pipe(
      tap(entries => this.mealPlan$.next(entries))
    );
  }

  addMealPlanEntry(request: CreateMealPlanEntryRequest): Observable<MealPlanEntry> {
    return this.http.post<MealPlanEntry>(`${this.baseUrl}/meal-plan`, request).pipe(
      tap(entry => {
        const current = this.mealPlan$.value;
        this.mealPlan$.next([...current, entry]);
      })
    );
  }

  deleteMealPlanEntry(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/meal-plan/${id}`).pipe(
      tap(() => {
        const current = this.mealPlan$.value;
        this.mealPlan$.next(current.filter(e => e.id !== id));
      })
    );
  }
}
