import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { AnimalService } from 'src/app/services/animal.service';
import { ToastrService } from 'ngx-toastr';
import {
  ADD_ANIMAL,
  ADD_ANIMAL_FAILURE,
  ADD_ANIMAL_SUCCESS,
  DELETE_ANIMAL,
  DELETE_ANIMAL_FAILURE,
  DELETE_ANIMAL_SUCCESS,
  GET_ALL_ANIMALS,
  GET_ALL_ANIMALS_FAILURE,
  GET_ALL_ANIMALS_SUCCESS,
  GET_ANIMAL,
  GET_ANIMAL_FAILURE,
  GET_ANIMAL_SUCCESS,
  SEARCH_ANIMAL_NAME,
  SEARCH_ANIMAL_NAME_FAILURE,
  SEARCH_ANIMAL_NAME_SUCCESS,
  UPDATE_ANIMAL,
  UPDATE_ANIMAL_FAILURE,
  UPDATE_ANIMAL_SUCCESS,
} from '../actions/animal.actions';
import { catchError, map, of, switchMap } from 'rxjs';
import { Animal, AnimalResult } from 'src/app/features/models/animal.models';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable()
export class AnimalEffects {
  constructor(
    private actions$: Actions,
    private animalService: AnimalService,
    private toastr: ToastrService
  ) { }

  addAnimal$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ADD_ANIMAL),
      switchMap(({ animal }) =>
        this.animalService.addAnimal(animal).pipe(
          map((animal: Animal) => {
            this.toastr.success('The animal was successfully added!');
            return ADD_ANIMAL_SUCCESS({ animal });
          }),
          catchError((error: HttpErrorResponse) => {
            const message =
              error.error?.message || error.message || 'Server error';
            this.toastr.error(
              `An error occurred while saving the animal: ${message}`
            );
            return of(ADD_ANIMAL_FAILURE({ error: message }));
          })
        )
      )
    )
  );

  getAllAnimals$ = createEffect(() =>
    this.actions$.pipe(
      ofType(GET_ALL_ANIMALS),
      switchMap(({ animalPagination, speciesId, sortBy }) =>
        this.animalService.getAllAnimals(animalPagination, speciesId, sortBy).pipe(
          map((animalResult: AnimalResult) =>
            GET_ALL_ANIMALS_SUCCESS({ animalResult })
          ),
          catchError((error: HttpErrorResponse) => {
            const message =
              error.error?.message || error.message || 'Server error';
            this.toastr.error(
              `An error occurred while fetching the animals: ${message}`
            );
            return of(GET_ALL_ANIMALS_FAILURE({ error: message }));
          })
        )
      )
    )
  );

  getAnimal$ = createEffect(() =>
    this.actions$.pipe(
      ofType(GET_ANIMAL),
      switchMap(({ animalId }) =>
        this.animalService.getAnimal(animalId).pipe(
          map((animal: Animal) => GET_ANIMAL_SUCCESS({ animal })),
          catchError((error: HttpErrorResponse) => {
            const message =
              error.error?.message || error.message || 'Server error';
            this.toastr.error(
              `An error occurred while fetching the animal: ${message}`
            );
            return of(GET_ANIMAL_FAILURE({ error: message }));
          })
        )
      )
    )
  );

  deleteAnimal$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DELETE_ANIMAL),
      switchMap(({ animalId }) =>
        this.animalService.deleteAnimal(animalId).pipe(
          map(() => {
            this.toastr.success('The animal was successfully deleted!');
            return DELETE_ANIMAL_SUCCESS({ animalId });
          }),
          catchError((error: HttpErrorResponse) => {
            const message =
              error.error?.message || error.message || 'Server error';
            this.toastr.error(
              `An error occurred while deleting the animal: ${message}`
            );
            return of(DELETE_ANIMAL_FAILURE({ error: message }));
          })
        )
      )
    )
  );

  updateAnimal$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UPDATE_ANIMAL),
      switchMap(({ animal }) =>
        this.animalService.updateAnimal(animal).pipe(
          map((animal: Animal) => {
            this.toastr.success('The animal was successfully updated!');
            return UPDATE_ANIMAL_SUCCESS({ animal });
          }),
          catchError((error: string) => {
            this.toastr.error(
              `An error occurred while updating the animal: ${error}`
            );
            return of(UPDATE_ANIMAL_FAILURE({ error }));
          })
        )
      )
    )
  );

  searchAnimal$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SEARCH_ANIMAL_NAME),
      switchMap(({ name }) => this.animalService.searchAnimal(name).pipe(
        map((animalResult: AnimalResult) => {
          return SEARCH_ANIMAL_NAME_SUCCESS({ animalResult })
        }),
        catchError((error: string) => {
          this.toastr.error(
            `An error occurred while searching the animal: ${error}`
          );
          return of(SEARCH_ANIMAL_NAME_FAILURE({ error }));
        })))
    ))
}
