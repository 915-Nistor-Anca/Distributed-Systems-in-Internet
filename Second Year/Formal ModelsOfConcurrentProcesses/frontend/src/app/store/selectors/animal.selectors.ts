import { createFeatureSelector, createSelector } from '@ngrx/store';
import { IAnimalState } from '../reducers/animal.reducers';

export const SELECT_ANIMAL_STATE =
  createFeatureSelector<IAnimalState>('animal');
export const SELECT_ANIMAL = createSelector(
  SELECT_ANIMAL_STATE,
  (state: IAnimalState) => state.animal
);
export const SELECT_ANIMAL_RESULT = createSelector(
  SELECT_ANIMAL_STATE,
  (state: IAnimalState) => state.animalResult
);
// export const SELECT_SEARCHED_ANIMAL = createSelector(
//   SELECT_ANIMAL_STATE,
//   (state: IAnimalState) => state.searchedAnimals
// )
