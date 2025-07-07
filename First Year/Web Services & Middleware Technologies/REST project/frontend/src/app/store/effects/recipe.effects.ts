import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { RecipeService } from 'src/app/services/recipe.service';
import { ToastrService } from 'ngx-toastr';
import {
  ADD_RECIPE,
  ADD_RECIPE_FAILURE,
  ADD_RECIPE_SUCCESS,
  DELETE_RECIPE,
  DELETE_RECIPE_FAILURE,
  DELETE_RECIPE_SUCCESS,
  GET_ALL_RECIPES,
  GET_ALL_RECIPES_FAILURE,
  GET_ALL_RECIPES_SUCCESS,
  GET_RECIPE,
  GET_RECIPE_FAILURE,
  GET_RECIPE_SUCCESS,
  UPDATE_RECIPE,
  UPDATE_RECIPE_FAILURE,
  UPDATE_RECIPE_SUCCESS,
} from '../actions/recipe.actions';
import { catchError, map, of, switchMap } from 'rxjs';
import { Recipe, RecipeResult } from 'src/app/features/models/recipe.models';

@Injectable()
export class RecipeEffects {
  constructor(
    private actions$: Actions,
    private recipeService: RecipeService,
    private toastr: ToastrService
  ) {}

  addRecipe$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ADD_RECIPE),
      switchMap(({ recipe }) =>
        this.recipeService.addRecipe(recipe).pipe(
          map((recipeId: number) => {
            this.toastr.success('The recipe was successfully added!');
            return ADD_RECIPE_SUCCESS({ recipeId, recipe });
          }),
          catchError((error: string) => {
            this.toastr.error(
              `An error occurred while saving the recipe: ${error}`
            );
            return of(ADD_RECIPE_FAILURE({ error }));
          })
        )
      )
    )
  );

  getAllRecipes = createEffect(() =>
    this.actions$.pipe(
      ofType(GET_ALL_RECIPES),
      switchMap(({ recipePagination }) =>
        this.recipeService.getAllRecipes(recipePagination).pipe(
          map((recipeResult: RecipeResult) =>
            GET_ALL_RECIPES_SUCCESS({ recipeResult })
          ),
          catchError((error: string) => {
            this.toastr.error(
              `An error occurred while fetching the recipes: ${error}`
            );
            return of(GET_ALL_RECIPES_FAILURE({ error }));
          })
        )
      )
    )
  );

  getRecipe$ = createEffect(() =>
    this.actions$.pipe(
      ofType(GET_RECIPE),
      switchMap(({ recipeId }) =>
        this.recipeService.getRecipe(recipeId).pipe(
          map((recipe: Recipe) => GET_RECIPE_SUCCESS({ recipe })),
          catchError((error: string) => {
            this.toastr.error(
              `An error occurred while fetching the recipe: ${error}`
            );
            return of(GET_RECIPE_FAILURE({ error }));
          })
        )
      )
    )
  );

  deleteRecipe$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DELETE_RECIPE),
      switchMap(({ recipeId }) =>
        this.recipeService.deleteRecipe(recipeId).pipe(
          map(() => {
            this.toastr.success('The recipe was successfully deleted!');
            return DELETE_RECIPE_SUCCESS({ recipeId });
          }),
          catchError((error: string) => {
            this.toastr.error(
              `An error occurred while deleting the recipe: ${error}`
            );
            return of(DELETE_RECIPE_FAILURE({ error }));
          })
        )
      )
    )
  );

  updateRecipe$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UPDATE_RECIPE),
      switchMap(({ recipe }) =>
        this.recipeService.updateRecipe(recipe).pipe(
          map(() => {
            this.toastr.success('The recipe was successfully updated!');
            return UPDATE_RECIPE_SUCCESS({ recipe });
          }),
          catchError((error: string) => {
            this.toastr.error(
              `An error occurred while updating the recipe: ${error}`
            );
            return of(UPDATE_RECIPE_FAILURE({ error }));
          })
        )
      )
    )
  );
}
