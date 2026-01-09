import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { ToastrService } from "ngx-toastr";
import { ADD_OWNER, ADD_OWNER_FAILURE, ADD_OWNER_SUCCESS, GET_ALL_OWNERS, GET_ALL_OWNERS_FAILURE, GET_ALL_OWNERS_SUCCESS } from "../actions/owner.actions";
import { catchError, map, of, switchMap } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { OwnerService } from "src/app/services/owner.service";
import { Owner, OwnerResult } from "src/app/features/models/owner.models";

@Injectable()
export class OwnerEffects {
  constructor(
    private actions$: Actions,
    private ownerService: OwnerService,
    private toastr: ToastrService
  ) {}

  getAllOwners = createEffect(() =>
    this.actions$.pipe(
      ofType(GET_ALL_OWNERS),
      switchMap(({ ownerPagination }) =>
        this.ownerService.getAllOwners(ownerPagination).pipe(
          map((ownerResult: OwnerResult) =>
            GET_ALL_OWNERS_SUCCESS({ ownerResult })
          ),
          catchError((error: HttpErrorResponse) => {
            const message =
            error.error?.message || error.message || 'Server error';
            this.toastr.error(
              `An error occurred while fetching the Owners: ${message}`
            );
            return of(GET_ALL_OWNERS_FAILURE({ error: message }));
          })
        )
      )
    )
  );

  addOwner$ = createEffect(() =>
      this.actions$.pipe(
        ofType(ADD_OWNER),
        switchMap(({ owner }) =>
          this.ownerService.addOwner(owner).pipe(
            map((owner: Owner) => {
              this.toastr.success('The owner was successfully added!');
              return ADD_OWNER_SUCCESS({ owner });
            }),
            catchError((error: HttpErrorResponse) => {
              const message =
                error.error?.message || error.message || 'Server error';
              this.toastr.error(
                `An error occurred while saving the owner: ${message}`
              );
              return of(ADD_OWNER_FAILURE({ error: message }));
            })
          )
        )
      )
    );
}