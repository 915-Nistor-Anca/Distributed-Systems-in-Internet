import { AfterViewInit, Component, inject, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { AddAnimalDialogComponent } from '../add-animal-dialog/add-animal-dialog.component';
import { Animal, AnimalResult } from '../models/animal.models';
import { DELETE_ANIMAL, GET_ALL_ANIMALS, UPDATE_ANIMAL } from 'src/app/store/actions/animal.actions';
import { SELECT_ANIMAL_RESULT } from 'src/app/store/selectors/animal.selectors';

@Component({
  selector: 'app-all-animals-page',
  templateUrl: './all-animals-page.component.html',
  styleUrls: ['./all-animals-page.component.scss'],
})
export class AllAnimalsPageComponent implements AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  dialog: MatDialog = inject(MatDialog);
  animalResult$: Observable<AnimalResult>;
  animalColumns: string[] = [
    'name',
    'species',
    'gender',
    'birthDate',
    'actions',
  ];
  isEditMode: true | false = false;
  editedAnimal: Animal | null = null;
  editedRowId: number | null = null;

  constructor(private store: Store) {
    this.animalResult$ = this.store.select(SELECT_ANIMAL_RESULT);
  }

  ngAfterViewInit() {
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
      })
    );
  }

  editAnimal(animal: Animal, rowId: number): void {
    this.isEditMode = true;
    this.editedAnimal = { ...animal };
    this.editedRowId = rowId;
  }

  saveAnimal(): void {
    if (this.editedAnimal) {
      this.store.dispatch(UPDATE_ANIMAL({ animal: this.editedAnimal }));
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
}
