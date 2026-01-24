import { createAction, props } from "@ngrx/store";
import { Owner, OwnerPagination, OwnerResult } from "src/app/features/models/owner.models";

export const GET_ALL_OWNERS = createAction(
    '[Owner] Get All Owners',
    props<{ ownerPagination: OwnerPagination }>()
);

export const GET_ALL_OWNERS_SUCCESS = createAction(
  '[Owner] Get All Owners Success',
  props<{ ownerResult: OwnerResult }>()
);

export const GET_ALL_OWNERS_FAILURE = createAction(
  '[Owner] Get All Owners Failure',
  props<{ error: string }>()
);

export const ADD_OWNER = createAction(
  '[Owner] Add Owner',
  props<{ owner: Owner }>()
);

export const ADD_OWNER_SUCCESS = createAction(
  '[Owner] Add Owner Success',
  props<{ owner: Owner }>()
);

export const ADD_OWNER_FAILURE = createAction(
  '[Owner] Add Owner Failure',
  props<{ error: string }>()
);