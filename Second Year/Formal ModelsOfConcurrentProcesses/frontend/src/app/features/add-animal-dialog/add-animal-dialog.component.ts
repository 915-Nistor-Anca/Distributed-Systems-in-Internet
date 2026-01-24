import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Animal, UpdateAnimal } from '../models/animal.models';
import { ADD_ANIMAL } from 'src/app/store/actions/animal.actions';
import { OwnerResult } from '../models/owner.models';
import { SpeciesResult } from '../models/species.models';
import { Observable } from 'rxjs';
import { SELECT_OWNERS_RESULT } from 'src/app/store/selectors/owner.selectors';
import { SELECT_SPECIES_RESULT } from 'src/app/store/selectors/species.selectors';
import { GET_ALL_OWNERS } from 'src/app/store/actions/owner.actions';
import { GET_ALL_SPECIES } from 'src/app/store/actions/species.actions';

@Component({
  selector: 'app-add-animal-dialog',
  templateUrl: './add-animal-dialog.component.html',
  styleUrls: ['./add-animal-dialog.component.scss'],
})
export class AddAnimalDialogComponent {
  dialogRef = inject(MatDialogRef<AddAnimalDialogComponent>);
  animalForm: FormGroup;
  formSubmitted: boolean = false;

  ownerResult$: Observable<OwnerResult>;
  speciesResult$: Observable<SpeciesResult>;

  constructor(private formBuilder: FormBuilder, private store: Store) {
    this.ownerResult$ = this.store.select(SELECT_OWNERS_RESULT);
    this.speciesResult$ = this.store.select(SELECT_SPECIES_RESULT);

    this.store.dispatch(GET_ALL_OWNERS({ ownerPagination: { pageNumber: 1, pageSize: 100 } }));
    this.store.dispatch(GET_ALL_SPECIES({ speciesPagination: { pageNumber: 1, pageSize: 100 } }));

    this.animalForm = this.formBuilder.group({
      name: ['', Validators.required],
      species: [null, Validators.required],
      gender: ['', Validators.required],
      birthDate: [null, Validators.required],
      owner: [null, Validators.required]  
    });
  }

  compareById(a: any, b: any): boolean {
    return a && b ? a.id === b.id : a === b;
  }

  onSubmit(): void {
    this.formSubmitted = true;

    if (this.animalForm.valid) {
      const formValue = this.animalForm.value;

      const animal: UpdateAnimal = {
        id: 0, 
        name: formValue.name,
        speciesId: formValue.species.id,
        gender: formValue.gender,
        birthDate: formValue.birthDate,
        ownerId: formValue.owner.id
      };

      this.store.dispatch(ADD_ANIMAL({ animal }));
      this.dialogRef.close();
    } else {
      this.animalForm.markAllAsTouched();
    }
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
