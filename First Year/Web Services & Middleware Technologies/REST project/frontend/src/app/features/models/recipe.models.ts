export interface Recipe {
  id: number;
  name: string;
  preparationSteps: string;
  ingredients: string;
  isVegetarian: boolean;
  preparationDate: Date;
}

export type RecipePagination = {
  pageNumber: number;
  pageSize: number;
};

export interface RecipeResult {
  items: Recipe[];
  totalCount: number;
}
