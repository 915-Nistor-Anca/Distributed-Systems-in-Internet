import { Owner } from "./owner.models";
import { Species } from "./species.models";

export interface Animal {
  id: number;
  name: string;
  species: Species;
  gender: string;
  birthDate: Date;
  owner: Owner;
}

export type AnimalPagination = {
  pageNumber: number;
  pageSize: number;
};

export interface AnimalResult {
  items: Animal[];
  totalCount: number;
}
