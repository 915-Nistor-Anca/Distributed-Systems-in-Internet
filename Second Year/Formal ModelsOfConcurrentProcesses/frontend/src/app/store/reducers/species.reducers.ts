import { createReducer, on } from "@ngrx/store";
import { Species, SpeciesResult } from "src/app/features/models/species.models";
import { GET_ALL_SPECIE_FAILURE, GET_ALL_SPECIES_SUCCESS } from "../actions/species.actions";

export interface ISpeciesState {
  speciesResult: SpeciesResult;
  species: Species;
  error: string;
}

export const INITIAL_STATE: ISpeciesState = {
  speciesResult: {
    items: [],
  },
  species: {
    id: 0,
    name: '',
   isExotic: false
  },
  error: '',
};

export const SPECIES_REDUCER = createReducer(
  INITIAL_STATE,
  on(GET_ALL_SPECIES_SUCCESS, (state: ISpeciesState, { speciesResult }) => ({
    ...state,
    speciesResult,
  })),
  on(GET_ALL_SPECIE_FAILURE, (state: ISpeciesState, { error }) => ({
    ...state,
    error: error,
  })),
);
