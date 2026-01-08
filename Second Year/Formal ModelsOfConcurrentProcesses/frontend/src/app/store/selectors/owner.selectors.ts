import { createFeatureSelector, createSelector } from "@ngrx/store";
import { IOwnerState } from "../reducers/species.effects";

export const SELECT_OWNERS_STATE =
  createFeatureSelector<IOwnerState>('owners');
export const SELECT_OWNERS_RESULT = createSelector(
  SELECT_OWNERS_STATE,
  (state: IOwnerState) => state.ownerResult
);
