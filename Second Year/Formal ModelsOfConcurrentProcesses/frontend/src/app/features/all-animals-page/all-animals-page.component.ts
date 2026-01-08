import { AfterViewInit, Component, inject, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { AddAnimalDialogComponent } from '../add-animal-dialog/add-animal-dialog.component';
import { Animal, AnimalResult, UpdateAnimal } from '../models/animal.models';
import { DELETE_ANIMAL, GET_ALL_ANIMALS, SEARCH_ANIMAL_NAME, UPDATE_ANIMAL } from 'src/app/store/actions/animal.actions';
import { SELECT_ANIMAL_RESULT } from 'src/app/store/selectors/animal.selectors';
import { OwnerResult } from '../models/owner.models';
import { SpeciesResult } from '../models/species.models';
import { SELECT_OWNERS_RESULT } from 'src/app/store/selectors/owner.selectors';
import { SELECT_SPECIES_RESULT } from 'src/app/store/selectors/species.selectors';
import { GET_ALL_OWNERS } from 'src/app/store/actions/owner.actions';
import { GET_ALL_SPECIES } from 'src/app/store/actions/species.actions';

@Component({
  selector: 'app-all-animals-page',
  templateUrl: './all-animals-page.component.html',
  styleUrls: ['./all-animals-page.component.scss'],
})
export class AllAnimalsPageComponent implements AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  dialog: MatDialog = inject(MatDialog);
  animalResult$: Observable<AnimalResult>;
  ownerResult$: Observable<OwnerResult>;
  speciesResult$: Observable<SpeciesResult>;
  animalColumns: string[] = [
    'name',
    'species',
    'gender',
    'birthDate',
    'owner',
    'actions',
  ];
  isEditMode: true | false = false;
  editedAnimal: Animal | null = null;
  editedRowId: number | null = null;
  selectedSpeciesId: number | null = null;

  constructor(private store: Store) {
    this.animalResult$ = this.store.select(SELECT_ANIMAL_RESULT);
    this.ownerResult$ = this.store.select(SELECT_OWNERS_RESULT);
    this.speciesResult$ = this.store.select(SELECT_SPECIES_RESULT);
  }

  ngAfterViewInit() {
    this.store.dispatch(
      GET_ALL_OWNERS({
        ownerPagination: {
          pageNumber: 1,
          pageSize: 100
        }
      })
    )
    console.log("dispatch")
    this.store.dispatch(
      GET_ALL_SPECIES({
        speciesPagination: {
          pageNumber: 1,
          pageSize: 100
        }
      })
    )
    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 5;
    this.setPagination();
    this.paginator.page.subscribe(() => {
      this.setPagination();
    });
  }

  setPagination(): void {
    this.store.dispatch(
      GET_ALL_ANIMALS({
        animalPagination: {
          pageNumber: this.paginator.pageIndex + 1,
          pageSize: this.paginator.pageSize,
        },
        speciesId: null
      })
    );
  }

  editAnimal(animal: Animal, rowId: number): void {
    this.isEditMode = true;
    this.editedAnimal = { ...animal,
      species: {...animal.species},
      owner: {...animal.owner}
     };
      console.log(this.editedAnimal)
    this.editedRowId = rowId;
  }

  compareById(a: any, b: any): boolean {
  return a && b ? a.id === b.id : a === b;
}

  saveAnimal(): void {
    if (this.editedAnimal) {
      console.log(this.editedAnimal)
      var updatedAnimal: UpdateAnimal = { id: this.editedAnimal.id,
         name: this.editedAnimal.name, speciesId: this.editedAnimal.species.id, 
         ownerId: this.editedAnimal.owner.id, gender: this.editedAnimal.gender,
        birthDate: this.editedAnimal.birthDate }
      this.store.dispatch(UPDATE_ANIMAL({ animal: updatedAnimal }));
    }
    this.cancelEdit();
  }

  deleteAnimal(animalId: number): void {
    this.store.dispatch(DELETE_ANIMAL({ animalId }));
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.editedAnimal = null;
    this.editedRowId = null;
  }

  viewAddAnimalDialog(): void {
    this.dialog.open(AddAnimalDialogComponent);
  }

  searchName: string = '';

onSearchNameChange(value: string): void {
  this.searchName = value;

  this.store.dispatch(SEARCH_ANIMAL_NAME({ name: this.searchName }));
  if (this.paginator) {
    this.paginator.pageIndex = 1;
  }
}

clearSearch(): void {
  this.searchName = '';
  this.store.dispatch(SEARCH_ANIMAL_NAME({ name: '' }));

  if (this.paginator) {
    this.paginator.pageIndex = 1;
  }
}

onSpeciesChange(): void {
this.store.dispatch(
      GET_ALL_ANIMALS({
        animalPagination: {
          pageNumber: 1,
          pageSize: 100,
        },
        speciesId: this.selectedSpeciesId
      })
    );
}

}
