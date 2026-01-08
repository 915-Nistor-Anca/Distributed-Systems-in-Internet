import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Owner,
  OwnerPagination,
  OwnerResult,
} from '../features/models/owner.models';

@Injectable({
  providedIn: 'root',
})
export class OwnerService {
  private apiUrl = 'https://localhost:7263/api';

  constructor(private httpClient: HttpClient) {}

  addOwner(owner: Owner): Observable<number> {
    return this.httpClient.post<number>(`${this.apiUrl}/Owner`, owner);
  }

  getOwner(ownerId: number): Observable<Owner> {
    return this.httpClient.get<Owner>(`${this.apiUrl}/Owner/${ownerId}`);
  }

  getAllOwners(ownerPagination: OwnerPagination): Observable<OwnerResult> {
    console.log("get all owners")
    return this.httpClient.get<OwnerResult>(`${this.apiUrl}/Owner`, {
      params: ownerPagination,
    });
  }

  deleteOwner(ownerId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.apiUrl}/Owner/${ownerId}`);
  }

  updateOwner(owner: Owner): Observable<void> {
    return this.httpClient.put<void>(`${this.apiUrl}/Owner`, owner);
  }
}
