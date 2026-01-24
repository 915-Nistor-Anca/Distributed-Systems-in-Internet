import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Animal,
  AnimalPagination,
  AnimalResult,
  UpdateAnimal,
} from '../features/models/animal.models';

@Injectable({
  providedIn: 'root',
})
export class AnimalService {
  private apiUrl = 'https://localhost:7263/api';

  constructor(private httpClient: HttpClient) { }

  addAnimal(animal: UpdateAnimal): Observable<Animal> {
    return this.httpClient.post<Animal>(`${this.apiUrl}/Animal`, animal);
  }

  getAnimal(animalId: number): Observable<Animal> {
    return this.httpClient.get<Animal>(`${this.apiUrl}/Animal/${animalId}`);
  }

  getAllAnimals(
    animalPagination: AnimalPagination,
    speciesId: number | null,
    sortBy: string | null
  ): Observable<AnimalResult> {
    const params = new HttpParams()
      .set('pageNumber', animalPagination.pageNumber.toString())
      .set('pageSize', animalPagination.pageSize.toString());
    var finalParams = speciesId !== null ? params.set('speciesId', speciesId.toString()) : params;
    finalParams = sortBy !== null ? params.set('sortBy', sortBy) : finalParams;
    return this.httpClient.get<AnimalResult>(`${this.apiUrl}/Animal`, { params: finalParams });
  }

  deleteAnimal(animalId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.apiUrl}/Animal/${animalId}`);
  }

  updateAnimal(animal: UpdateAnimal): Observable<Animal> {
    return this.httpClient.put<Animal>(`${this.apiUrl}/Animal`, animal);
  }

  searchAnimal(name: string): Observable<AnimalResult> {
    return this.httpClient.get<AnimalResult>(`${this.apiUrl}/Animal/search?name=${name}`);
  }
}
