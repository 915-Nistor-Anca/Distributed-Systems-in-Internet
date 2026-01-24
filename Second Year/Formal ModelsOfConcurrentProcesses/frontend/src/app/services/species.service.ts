import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Species, SpeciesPagination, SpeciesResult } from '../features/models/species.models';

@Injectable({
  providedIn: 'root',
})
export class SpecieService {
  private apiUrl = 'https://localhost:7263/api';

  constructor(private httpClient: HttpClient) {}

  addSpecie(species: Species): Observable<number> {
    return this.httpClient.post<number>(`${this.apiUrl}/Species`, species);
  }

  getSpecie(speciesId: number): Observable<Species> {
    return this.httpClient.get<Species>(`${this.apiUrl}/Species/${speciesId}`);
  }

  getAllSpecies(speciesPagination: SpeciesPagination): Observable<SpeciesResult> {
    console.log("get all species")
    return this.httpClient.get<SpeciesResult>(`${this.apiUrl}/Species`, {
      params: speciesPagination,
    });
  }

  deleteSpecie(speciesId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.apiUrl}/Species/${speciesId}`);
  }

  updateSpecie(species: Species): Observable<void> {
    return this.httpClient.put<void>(`${this.apiUrl}/Species`, species);
  }
}
