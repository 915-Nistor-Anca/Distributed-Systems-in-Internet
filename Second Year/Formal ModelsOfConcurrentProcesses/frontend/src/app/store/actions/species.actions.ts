import { createAction, props } from "@ngrx/store";
import { SpeciesPagination, SpeciesResult } from "src/app/features/models/species.models";

export const GET_ALL_SPECIES = createAction(
    '[Species] Get All Species',
    props<{ speciesPagination: SpeciesPagination }>()
);
export const GET_ALL_SPECIES_SUCCESS = createAction(
  '[Species] Get All Species Success',
  props<{ speciesResult: SpeciesResult }>()
);

export const GET_ALL_SPECIE_FAILURE = createAction(
  '[Species] Get All Species Failure',
  props<{ error: string }>()
);