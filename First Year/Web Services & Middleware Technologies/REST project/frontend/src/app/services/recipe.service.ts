import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Recipe,
  RecipePagination,
  RecipeResult,
} from '../features/models/recipe.models';

@Injectable({
  providedIn: 'root',
})
export class RecipeService {
  private apiUrl = 'http://localhost:5236/api';

  constructor(private httpClient: HttpClient) {}

  addRecipe(recipe: Recipe): Observable<number> {
    return this.httpClient.post<number>(`${this.apiUrl}/Recipe`, recipe);
  }

  getRecipe(recipeId: number): Observable<Recipe> {
    return this.httpClient.get<Recipe>(`${this.apiUrl}/Recipe/${recipeId}`);
  }

  getAllRecipes(recipePagination: RecipePagination): Observable<RecipeResult> {
    return this.httpClient.get<RecipeResult>(`${this.apiUrl}/Recipe`, {
      params: recipePagination,
    });
  }

  deleteRecipe(recipeId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.apiUrl}/Recipe/${recipeId}`);
  }

  updateRecipe(recipe: Recipe): Observable<void> {
    return this.httpClient.put<void>(`${this.apiUrl}/Recipe`, recipe);
  }
}
