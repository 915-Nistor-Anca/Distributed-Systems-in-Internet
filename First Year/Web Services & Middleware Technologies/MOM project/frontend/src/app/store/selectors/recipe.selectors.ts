import { createFeatureSelector, createSelector } from '@ngrx/store';
import { IRecipeState } from '../reducers/recipe.reducers';

export const SELECT_RECIPE_STATE =
  createFeatureSelector<IRecipeState>('recipe');
export const SELECT_RECIPE = createSelector(
  SELECT_RECIPE_STATE,
  (state: IRecipeState) => state.recipe
);
export const SELECT_RECIPE_RESULT = createSelector(
  SELECT_RECIPE_STATE,
  (state: IRecipeState) => state.recipeResult
);
