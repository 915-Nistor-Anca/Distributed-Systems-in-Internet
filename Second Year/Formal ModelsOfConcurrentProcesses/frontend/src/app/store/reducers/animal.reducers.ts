import { Animal, AnimalResult } from 'src/app/features/models/animal.models';
import { createReducer, on } from '@ngrx/store';
import {
  ADD_ANIMAL_FAILURE,
  ADD_ANIMAL_SUCCESS,
  DELETE_ANIMAL_FAILURE,
  DELETE_ANIMAL_SUCCESS,
  GET_ALL_ANIMALS_FAILURE,
  GET_ALL_ANIMALS_SUCCESS,
  GET_ANIMAL_FAILURE,
  GET_ANIMAL_SUCCESS,
  SEARCH_ANIMAL_NAME_FAILURE,
  SEARCH_ANIMAL_NAME_SUCCESS,
  UPDATE_ANIMAL_FAILURE,
  UPDATE_ANIMAL_SUCCESS,
} from '../actions/animal.actions';

export interface IAnimalState {
  animalResult: AnimalResult;
  animal: Animal;
  error: string;
 // searchedAnimals: AnimalResult;
}

export const INITIAL_STATE: IAnimalState = {
  animalResult: {
    items: [],
    totalCount: 0,
  },
  animal: {
    id: 0,
    name: '',
    species: {id: 0, name: '', isExotic: false},
    gender: '',
    birthDate: new Date(),
    owner: {id: 0, name: '', phoneNumber: ''}
  },
  // searchedAnimals: {
  //    items: [],
  //   totalCount: 0,
  // },
  error: '',
};

export const ANIMAL_REDUCER = createReducer(
  INITIAL_STATE,
  on(ADD_ANIMAL_SUCCESS, (state: IAnimalState, { animal }) => ({
    ...state,
    animalResult: {
      ...state.animalResult,
      items: [...state.animalResult.items, { ...animal }],
      totalCount: state.animalResult.totalCount + 1,
    },
  })),
  on(GET_ALL_ANIMALS_SUCCESS, (state: IAnimalState, { animalResult }) => ({
    ...state,
    animalResult,
  })),
  on(GET_ANIMAL_SUCCESS, (state: IAnimalState, { animal }) => ({
    ...state,
    animal,
  })),
  on(UPDATE_ANIMAL_SUCCESS, (state: IAnimalState, { animal }) => ({
    ...state,
    animalResult: {
      ...state.animalResult,
      items: state.animalResult.items.map((a: Animal) =>
        a.id === animal.id ? { ...animal } : a
      ),
    },
  })),
  on(DELETE_ANIMAL_SUCCESS, (state: IAnimalState, { animalId }) => ({
    ...state,
    animalResult: {
      ...state.animalResult,
      items: state.animalResult.items.filter(
        (animal: Animal) => animal.id !== animalId
      ),
      totalCount: state.animalResult.totalCount - 1,
    },
  })),
  on(ADD_ANIMAL_FAILURE, (state: IAnimalState, { error }) => ({
    ...state,
    error: error,
  })),
  on(GET_ALL_ANIMALS_FAILURE, (state: IAnimalState, { error }) => ({
    ...state,
    error: error,
  })),
  on(GET_ANIMAL_FAILURE, (state: IAnimalState, { error }) => ({
    ...state,
    error: error,
  })),
  on(UPDATE_ANIMAL_FAILURE, (state: IAnimalState, { error }) => ({
    ...state,
    error: error,
  })),
  on(DELETE_ANIMAL_FAILURE, (state: IAnimalState, { error }) => ({
    ...state,
    error: error,
  })),
on(SEARCH_ANIMAL_NAME_SUCCESS, (state: IAnimalState, {animalResult})=>({
  ...state,
  animalResult
})),
on(SEARCH_ANIMAL_NAME_FAILURE, (state: IAnimalState, { error }) => ({
    ...state,
    error: error,
  })),
);
