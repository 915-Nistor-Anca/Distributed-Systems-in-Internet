import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Animal } from '../models/animal.models';
import { ADD_ANIMAL } from 'src/app/store/actions/animal.actions';

@Component({
  selector: 'app-add-animal-dialog',
  templateUrl: './add-animal-dialog.component.html',
  styleUrls: ['./add-animal-dialog.component.scss'],
})
export class AddAnimalDialogComponent {
  dialogRef = inject(MatDialogRef<AddAnimalDialogComponent>);
  animalForm: FormGroup;
  formSubmitted: boolean = false;

  constructor(private formBuilder: FormBuilder, private store: Store) {
    this.animalForm = this.formBuilder.group({
      name: ['', Validators.required],
      species: ['', Validators.required],
      gender: ['', Validators.required],
      birthDate: ['', Validators.required],
      owner: ['', Validators.required]
    });
  }

  onSubmit(): void {
    this.formSubmitted = true;
    if (this.animalForm.valid) {
      const animal: Animal = {
        id: 0,
        name: this.animalForm.value.name,
        species: this.animalForm.value.species.id,
        gender: this.animalForm.value.gender,
        birthDate: this.animalForm.value.birthDate,
        owner: this.animalForm.value.owner.id
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
