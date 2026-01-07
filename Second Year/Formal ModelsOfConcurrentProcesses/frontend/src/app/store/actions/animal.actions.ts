import { createAction, props } from '@ngrx/store';
import {
  Animal,
  AnimalPagination,
  AnimalResult,
} from 'src/app/features/models/animal.models';

export const ADD_ANIMAL = createAction(
  '[Animal] Add Animal',
  props<{ animal: Animal }>()
);

export const ADD_ANIMAL_SUCCESS = createAction(
  '[Animal] Add Animal Success',
  props<{ animalId: number; animal: Animal }>()
);

export const ADD_ANIMAL_FAILURE = createAction(
  '[Animal] Add Animal Failure',
  props<{ error: string }>()
);

export const UPDATE_ANIMAL = createAction(
  '[Animal] Update Animal',
  props<{ animal: Animal }>()
);

export const UPDATE_ANIMAL_SUCCESS = createAction(
  '[Animal] Update Animal Success',
  props<{ animal: Animal }>()
);

export const UPDATE_ANIMAL_FAILURE = createAction(
  '[Animal] Update Animal Failure',
  props<{ error: string }>()
);

export const DELETE_ANIMAL = createAction(
  '[Animal] Delete Animal',
  props<{ animalId: number }>()
);

export const DELETE_ANIMAL_SUCCESS = createAction(
  '[Animal] Delete Animal Success',
  props<{ animalId: number }>()
);

export const DELETE_ANIMAL_FAILURE = createAction(
  '[Animal] Delete Animal Failure',
  props<{ error: string }>()
);

export const GET_ANIMAL = createAction(
  '[Animal] Get Animal',
  props<{ animalId: number }>()
);

export const GET_ANIMAL_SUCCESS = createAction(
  '[Animal] Get Animal Success',
  props<{ animal: Animal }>()
);

export const GET_ANIMAL_FAILURE = createAction(
  '[Animal] Get Animal Failure',
  props<{ error: string }>()
);

export const GET_ALL_ANIMALS = createAction(
  '[Animal] Get All Animals',
  props<{ animalPagination: AnimalPagination }>()
);

export const GET_ALL_ANIMALS_SUCCESS = createAction(
  '[Animal] Get All Animals Success',
  props<{ animalResult: AnimalResult }>()
);

export const GET_ALL_ANIMALS_FAILURE = createAction(
  '[Animal] Get All Animals Failure',
  props<{ error: string }>()
);
