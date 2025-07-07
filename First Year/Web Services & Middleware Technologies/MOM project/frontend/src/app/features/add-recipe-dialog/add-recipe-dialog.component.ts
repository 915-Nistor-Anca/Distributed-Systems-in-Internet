import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Recipe } from '../models/recipe.models';
import { ADD_RECIPE } from 'src/app/store/actions/recipe.actions';

@Component({
  selector: 'app-add-recipe-dialog',
  templateUrl: './add-recipe-dialog.component.html',
  styleUrls: ['./add-recipe-dialog.component.scss'],
})
export class AddRecipeDialogComponent {
  dialogRef = inject(MatDialogRef<AddRecipeDialogComponent>);
  recipeForm: FormGroup;
  formSubmitted: boolean = false;

  constructor(private formBuilder: FormBuilder, private store: Store) {
    this.recipeForm = this.formBuilder.group({
      name: ['', Validators.required],
      preparationSteps: ['', Validators.required],
      ingredients: ['', Validators.required],
      preparationDate: ['', Validators.required],
      isVegetarian: [false, Validators.required],
    });
  }

  onSubmit(): void {
    this.formSubmitted = true;
    if (this.recipeForm.valid) {
      const recipe: Recipe = {
        id: 0,
        name: this.recipeForm.value.name,
        preparationSteps: this.recipeForm.value.preparationSteps,
        ingredients: this.recipeForm.value.ingredients,
        preparationDate: this.recipeForm.value.preparationDate,
        isVegetarian: this.recipeForm.value.isVegetarian,
      };
      this.store.dispatch(ADD_RECIPE({ recipe }));
      this.dialogRef.close();
    } else {
      this.recipeForm.markAllAsTouched();
    }
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
