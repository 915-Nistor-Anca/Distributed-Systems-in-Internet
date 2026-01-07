export interface Animal {
  id: number;
  name: string;
  species: string;
  gender: string;
  birthDate: Date;
}

export type AnimalPagination = {
  pageNumber: number;
  pageSize: number;
};

export interface AnimalResult {
  items: Animal[];
  totalCount: number;
}
