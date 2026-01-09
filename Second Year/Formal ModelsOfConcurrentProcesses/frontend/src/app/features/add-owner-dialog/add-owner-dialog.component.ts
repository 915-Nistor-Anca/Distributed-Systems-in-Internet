import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { ADD_OWNER } from 'src/app/store/actions/owner.actions';
import { Owner } from '../models/owner.models';

@Component({
  selector: 'app-add-owner-dialog',
  templateUrl: './add-owner-dialog.component.html',
  styleUrls: ['./add-owner-dialog.component.scss'],
})
export class AddOwnerDialogComponent {
  dialogRef = inject(MatDialogRef<AddOwnerDialogComponent>);

  ownerForm: FormGroup;
  formSubmitted = false;

  constructor(
    private formBuilder: FormBuilder,
    private store: Store
  ) {
    this.ownerForm = this.formBuilder.group({
      name: ['', Validators.required],
      phoneNumber: ['', Validators.required],
    });
  }

  onSubmit(): void {
    this.formSubmitted = true;

    if (this.ownerForm.valid) {
      const owner: Owner = {
        id: 0,
        name: this.ownerForm.value.name,
        phoneNumber: this.ownerForm.value.phoneNumber,
      };

      this.store.dispatch(ADD_OWNER({ owner }));
      this.dialogRef.close();
    } else {
      this.ownerForm.markAllAsTouched();
    }
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
