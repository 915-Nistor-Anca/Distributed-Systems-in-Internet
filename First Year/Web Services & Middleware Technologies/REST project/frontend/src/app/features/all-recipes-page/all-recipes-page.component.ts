import { AfterViewInit, Component, inject, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { AddRecipeDialogComponent } from '../add-recipe-dialog/add-recipe-dialog.component';
import { Recipe, RecipeResult } from '../models/recipe.models';
import { DELETE_RECIPE, GET_ALL_RECIPES, UPDATE_RECIPE } from 'src/app/store/actions/recipe.actions';
import { SELECT_RECIPE_RESULT } from 'src/app/store/selectors/recipe.selectors';

@Component({
  selector: 'app-all-recipes-page',
  templateUrl: './all-recipes-page.component.html',
  styleUrls: ['./all-recipes-page.component.scss'],
})
export class AllRecipesPageComponent implements AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  dialog: MatDialog = inject(MatDialog);
  recipeResult$: Observable<RecipeResult>;
  recipeColumns: string[] = [
    'name',
    'preparationSteps',
    'ingredients',
    'preparationDate',
    'isVegetarian',
    'actions',
  ];
  isEditMode: true | false = false;
  editedRecipe: Recipe | null = null;
  editedRowId: number | null = null;

  constructor(private store: Store) {
    this.recipeResult$ = this.store.select(SELECT_RECIPE_RESULT);
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
      GET_ALL_RECIPES({
        recipePagination: {
          pageNumber: this.paginator.pageIndex + 1,
          pageSize: this.paginator.pageSize,
        },
      })
    );
  }

  editRecipe(recipe: Recipe, rowId: number): void {
    this.isEditMode = true;
    this.editedRecipe = { ...recipe };
    this.editedRowId = rowId;
  }

  saveRecipe(): void {
    if (this.editedRecipe) {
      this.store.dispatch(UPDATE_RECIPE({ recipe: this.editedRecipe }));
    }
    this.cancelEdit();
  }

  deleteRecipe(recipeId: number): void {
    this.store.dispatch(DELETE_RECIPE({ recipeId }));
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.editedRecipe = null;
    this.editedRowId = null;
  }

  viewAddRecipeDialog(): void {
    this.dialog.open(AddRecipeDialogComponent);
  }
}
