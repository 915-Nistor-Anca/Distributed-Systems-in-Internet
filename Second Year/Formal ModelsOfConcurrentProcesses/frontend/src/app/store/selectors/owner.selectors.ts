import { createFeatureSelector, createSelector } from "@ngrx/store";
import { IOwnerState } from "../reducers/owner.reducers";

export const SELECT_OWNERS_STATE =
  createFeatureSelector<IOwnerState>('owners');
export const SELECT_OWNERS_RESULT = createSelector(
  SELECT_OWNERS_STATE,
  (state: IOwnerState) => state.ownerResult
);
