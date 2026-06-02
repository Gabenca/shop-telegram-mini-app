import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Recipe, CreateRecipeRequest } from '../models';

import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Recipe, CreateRecipeRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class RecipeService {

  private recipes$ = new BehaviorSubject<Recipe[]>([]);

  constructor(private api: ApiService) {}

  getRecipesObservable(): Observable<Recipe[]> {
    return this.recipes$.asObservable();
  }

  loadRecipes(): Observable<Recipe[]> {
    return this.api.http.get<Recipe[]>(`${this.api.baseUrl}/recipes`).pipe(
      tap(recipes => this.recipes$.next(recipes))
    );
  }

  getRecipe(id: number): Observable<Recipe> {
    return this.api.http.get<Recipe>(`${this.api.baseUrl}/recipes/${id}`);
  }

  createRecipe(request: CreateRecipeRequest): Observable<Recipe> {
    return this.api.http.post<Recipe>(`${this.api.baseUrl}/recipes`, request).pipe(
      tap(recipe => {
        const current = this.recipes$.value;
        this.recipes$.next([...current, recipe]);
      })
    );
  }

  updateRecipe(id: number, request: CreateRecipeRequest): Observable<Recipe> {
    return this.api.http.put<Recipe>(`${this.api.baseUrl}/recipes/${id}`, request).pipe(
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
    return this.api.http.delete<void>(`${this.api.baseUrl}/recipes/${id}`).pipe(
      tap(() => {
        const current = this.recipes$.value;
        this.recipes$.next(current.filter(r => r.id !== id));
      })
    );
  }

  uploadPhoto(file: File): Observable<{ fileId: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.api.http.post<{ fileId: string }>(`${this.api.baseUrl}/recipes/upload-photo`, formData);
  }
}
