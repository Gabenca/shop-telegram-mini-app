import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Recipe, CreateRecipeRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class RecipeService extends ApiService {

  private recipes$ = new BehaviorSubject<Recipe[]>([]);

  getRecipesObservable(): Observable<Recipe[]> {
    return this.recipes$.asObservable();
  }

  loadRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.baseUrl}/recipes`).pipe(
      tap(recipes => this.recipes$.next(recipes))
    );
  }

  getRecipe(id: number): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.baseUrl}/recipes/${id}`);
  }

  createRecipe(request: CreateRecipeRequest): Observable<Recipe> {
    return this.http.post<Recipe>(`${this.baseUrl}/recipes`, request).pipe(
      tap(recipe => {
        const current = this.recipes$.value;
        this.recipes$.next([...current, recipe]);
      })
    );
  }

  updateRecipe(id: number, request: CreateRecipeRequest): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.baseUrl}/recipes/${id}`, request).pipe(
      tap(updatedRecipe => {
        const current = this.recipes$.value;
        const index = current.findIndex(r => r.id === id);
        if (index !== -1) {
          current[index] = updatedRecipe;
          this.recipes$.next([...current]);
        }
      })
    );
  }

  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/recipes/${id}`).pipe(
      tap(() => {
        const current = this.recipes$.value;
        this.recipes$.next(current.filter(r => r.id !== id));
      })
    );
  }

  uploadPhoto(file: File): Observable<{ fileId: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ fileId: string }>(`${this.baseUrl}/recipes/upload-photo`, formData);
  }
}
