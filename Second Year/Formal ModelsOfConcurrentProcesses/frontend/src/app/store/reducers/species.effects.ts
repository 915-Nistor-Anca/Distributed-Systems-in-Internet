import { createReducer, on } from "@ngrx/store";
import { Owner, OwnerResult } from "src/app/features/models/owner.models";
import { GET_ALL_OWNERS_FAILURE, GET_ALL_OWNERS_SUCCESS } from "../actions/owner.actions";

export interface IOwnerState {
  ownerResult: OwnerResult;
  owner: Owner;
  error: string;
}

export const INITIAL_STATE: IOwnerState = {
  ownerResult: {
    items: [],
  },
  owner: {
    id: 0,
    name: '',
    phoneNumber: ''
  },
  error: '',
};

export const OWNERS_REDUCER = createReducer(
  INITIAL_STATE,
  on(GET_ALL_OWNERS_SUCCESS, (state: IOwnerState, { ownerResult }) => ({
    ...state,
    ownerResult,
  })),
  on(GET_ALL_OWNERS_FAILURE, (state: IOwnerState, { error }) => ({
    ...state,
    error: error,
  })),
);
