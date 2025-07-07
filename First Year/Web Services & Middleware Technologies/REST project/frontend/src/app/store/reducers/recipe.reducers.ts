import { Recipe, RecipeResult } from 'src/app/features/models/recipe.models';
import { createReducer, on } from '@ngrx/store';
import {
  ADD_RECIPE_FAILURE,
  ADD_RECIPE_SUCCESS,
  DELETE_RECIPE_FAILURE,
  DELETE_RECIPE_SUCCESS,
  GET_ALL_RECIPES_FAILURE,
  GET_ALL_RECIPES_SUCCESS,
  GET_RECIPE_FAILURE,
  GET_RECIPE_SUCCESS,
  UPDATE_RECIPE_FAILURE,
  UPDATE_RECIPE_SUCCESS,
} from '../actions/recipe.actions';

export interface IRecipeState {
  recipeResult: RecipeResult;
  recipe: Recipe;
  error: string;
}

export const INITIAL_STATE: IRecipeState = {
  recipeResult: {
    items: [],
    totalCount: 0,
  },
  recipe: {
    id: 0,
    name: '',
    preparationSteps: '',
    ingredients: '',
    preparationDate: new Date(),
    isVegetarian: false,
  },
  error: '',
};

export const RECIPE_REDUCER = createReducer(
  INITIAL_STATE,
  on(ADD_RECIPE_SUCCESS, (state: IRecipeState, { recipeId, recipe }) => ({
    ...state,
    recipeResult: {
      ...state.recipeResult,
      items: [...state.recipeResult.items, { ...recipe, id: recipeId }],
      totalCount: state.recipeResult.totalCount + 1,
    },
  })),
  on(GET_ALL_RECIPES_SUCCESS, (state: IRecipeState, { recipeResult }) => ({
    ...state,
    recipeResult,
  })),
  on(GET_RECIPE_SUCCESS, (state: IRecipeState, { recipe }) => ({
    ...state,
    recipe,
  })),
  on(UPDATE_RECIPE_SUCCESS, (state: IRecipeState, { recipe }) => ({
    ...state,
    recipeResult: {
      ...state.recipeResult,
      items: state.recipeResult.items.map((a: Recipe) =>
        a.id === recipe.id ? { ...recipe } : a
      ),
    },
  })),
  on(DELETE_RECIPE_SUCCESS, (state: IRecipeState, { recipeId }) => ({
    ...state,
    recipeResult: {
      ...state.recipeResult,
      items: state.recipeResult.items.filter(
        (recipe: Recipe) => recipe.id !== recipeId
      ),
      totalCount: state.recipeResult.totalCount - 1,
    },
  })),
  on(ADD_RECIPE_FAILURE, (state: IRecipeState, { error }) => ({
    ...state,
    error: error,
  })),
  on(GET_ALL_RECIPES_FAILURE, (state: IRecipeState, { error }) => ({
    ...state,
    error: error,
  })),
  on(GET_RECIPE_FAILURE, (state: IRecipeState, { error }) => ({
    ...state,
    error: error,
  })),
  on(UPDATE_RECIPE_FAILURE, (state: IRecipeState, { error }) => ({
    ...state,
    error: error,
  })),
  on(DELETE_RECIPE_FAILURE, (state: IRecipeState, { error }) => ({
    ...state,
    error: error,
  }))
);
