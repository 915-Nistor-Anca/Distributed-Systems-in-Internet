import { createFeatureSelector, createSelector } from "@ngrx/store";
import { ISpeciesState } from "../reducers/owner.reducers";

export const SELECT_SPECIES_STATE =
  createFeatureSelector<ISpeciesState>('species');
export const SELECT_SPECIES_RESULT = createSelector(
  SELECT_SPECIES_STATE,
  (state: ISpeciesState) => state.speciesResult
);
