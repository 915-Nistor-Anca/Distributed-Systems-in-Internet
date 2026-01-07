import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Animal,
  AnimalPagination,
  AnimalResult,
} from '../features/models/animal.models';

@Injectable({
  providedIn: 'root',
})
export class AnimalService {
  private apiUrl = 'https://localhost:7263/api';

  constructor(private httpClient: HttpClient) {}

  addAnimal(animal: Animal): Observable<number> {
    return this.httpClient.post<number>(`${this.apiUrl}/Animal`, animal);
  }

  getAnimal(animalId: number): Observable<Animal> {
    return this.httpClient.get<Animal>(`${this.apiUrl}/Animal/${animalId}`);
  }

  getAllAnimals(animalPagination: AnimalPagination): Observable<AnimalResult> {
    return this.httpClient.get<AnimalResult>(`${this.apiUrl}/Animal`, {
      params: animalPagination,
    });
  }

  deleteAnimal(animalId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.apiUrl}/Animal/${animalId}`);
  }

  updateAnimal(animal: Animal): Observable<void> {
    return this.httpClient.put<void>(`${this.apiUrl}/Animal`, animal);
  }
}
