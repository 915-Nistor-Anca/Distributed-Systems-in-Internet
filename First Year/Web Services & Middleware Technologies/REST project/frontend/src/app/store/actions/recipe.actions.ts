import { createAction, props } from '@ngrx/store';
import {
  Recipe,
  RecipePagination,
  RecipeResult,
} from 'src/app/features/models/recipe.models';

export const ADD_RECIPE = createAction(
  '[Recipe] Add Recipe',
  props<{ recipe: Recipe }>()
);

export const ADD_RECIPE_SUCCESS = createAction(
  '[Recipe] Add Recipe Success',
  props<{ recipeId: number; recipe: Recipe }>()
);

export const ADD_RECIPE_FAILURE = createAction(
  '[Recipe] Add Recipe Failure',
  props<{ error: string }>()
);

export const UPDATE_RECIPE = createAction(
  '[Recipe] Update Recipe',
  props<{ recipe: Recipe }>()
);

export const UPDATE_RECIPE_SUCCESS = createAction(
  '[Recipe] Update Recipe Success',
  props<{ recipe: Recipe }>()
);

export const UPDATE_RECIPE_FAILURE = createAction(
  '[Recipe] Update Recipe Failure',
  props<{ error: string }>()
);

export const DELETE_RECIPE = createAction(
  '[Recipe] Delete Recipe',
  props<{ recipeId: number }>()
);

export const DELETE_RECIPE_SUCCESS = createAction(
  '[Recipe] Delete Recipe Success',
  props<{ recipeId: number }>()
);

export const DELETE_RECIPE_FAILURE = createAction(
  '[Recipe] Delete Recipe Failure',
  props<{ error: string }>()
);

export const GET_RECIPE = createAction(
  '[Recipe] Get Recipe',
  props<{ recipeId: number }>()
);

export const GET_RECIPE_SUCCESS = createAction(
  '[Recipe] Get Recipe Success',
  props<{ recipe: Recipe }>()
);

export const GET_RECIPE_FAILURE = createAction(
  '[Recipe] Get Recipe Failure',
  props<{ error: string }>()
);

export const GET_ALL_RECIPES = createAction(
  '[Recipe] Get All Recipes',
  props<{ recipePagination: RecipePagination }>()
);

export const GET_ALL_RECIPES_SUCCESS = createAction(
  '[Recipe] Get All Recipes Success',
  props<{ recipeResult: RecipeResult }>()
);

export const GET_ALL_RECIPES_FAILURE = createAction(
  '[Recipe] Get All Recipes Failure',
  props<{ error: string }>()
);
