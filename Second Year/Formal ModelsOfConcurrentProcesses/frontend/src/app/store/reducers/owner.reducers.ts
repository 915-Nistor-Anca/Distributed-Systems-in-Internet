import { createReducer, on } from "@ngrx/store";
import { Owner, OwnerResult } from "src/app/features/models/owner.models";
import { ADD_OWNER_SUCCESS, GET_ALL_OWNERS_FAILURE, GET_ALL_OWNERS_SUCCESS } from "../actions/owner.actions";

export interface IOwnerState {
  ownerResult: OwnerResult;
  owner: Owner;
  error: string;
}

export const INITIAL_STATE: IOwnerState = {
  ownerResult: {
    items: [],
    totalCount: 0
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
  on(ADD_OWNER_SUCCESS, (state: IOwnerState, { owner }) => ({
      ...state,
      ownerResult: {
        ...state.ownerResult,
        items: [...state.ownerResult.items, { ...owner }],
        totalCount: state.ownerResult.totalCount + 1
      },
    })),
);
