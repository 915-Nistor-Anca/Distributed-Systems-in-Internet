import { createAction, props } from "@ngrx/store";
import { OwnerPagination, OwnerResult } from "src/app/features/models/owner.models";

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