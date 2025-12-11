import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SoftwareDTO {
    id: number;
    name: string;
    version: string;
    description: string;
}

const BASE_URL = '/api/softwares';

@Injectable({
  providedIn: 'root'
})
export class SoftwareService {
  constructor(private readonly http: HttpClient) { }

  getAllSoftwares(): Observable<SoftwareDTO[]> {
    return this.http.get<SoftwareDTO[]>(BASE_URL);
  }
  getSoftware(id: number | string): Observable<SoftwareDTO> {
    return this.http.get<SoftwareDTO>(`${BASE_URL}/${id}`);
  }

  createSoftware(data: any): Observable<any> {
    return this.http.post(BASE_URL, data);
  }

  updateSoftware(id: number, data: any): Observable<any> {
    return this.http.put(`${BASE_URL}/${id}`, data);
  }

  deleteSoftware(id: number): Observable<any> {
    return this.http.delete(`${BASE_URL}/${id}`);
  }
}