import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { ToastrService } from "ngx-toastr";
import { GET_ALL_SPECIES, GET_ALL_SPECIE_FAILURE, GET_ALL_SPECIES_SUCCESS } from "../actions/species.actions";
import { catchError, map, of, switchMap } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { SpecieService } from "src/app/services/species.service";
import { SpeciesResult } from "src/app/features/models/species.models";

@Injectable()
export class SpecieEffects {
  constructor(
    private actions$: Actions,
    private SpecieService: SpecieService,
    private toastr: ToastrService
  ) {}

  getAllSpecies = createEffect(() =>
    this.actions$.pipe(
      ofType(GET_ALL_SPECIES),
      switchMap(({ speciesPagination }) =>
        this.SpecieService.getAllSpecies(speciesPagination).pipe(
          map((speciesResult: SpeciesResult) =>
            GET_ALL_SPECIES_SUCCESS({ speciesResult })
          ),
          catchError((error: HttpErrorResponse) => {
            const message =
            error.error?.message || error.message || 'Server error';
            this.toastr.error(
              `An error occurred while fetching the Species: ${message}`
            );
            return of(GET_ALL_SPECIE_FAILURE({ error: message }));
          })
        )
      )
    )
  );
}