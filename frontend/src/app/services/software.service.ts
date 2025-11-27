import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { LoginService } from '../login/login.service';

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
  constructor(private http: HttpClient,  private loginService: LoginService) { }

  getAllSoftwares(): Observable<SoftwareDTO[]> {
    return this.http.get<SoftwareDTO[]>(BASE_URL);
  }
  getSoftware(id: number | string): Observable<SoftwareDTO> {
    return this.http.get<SoftwareDTO>(`${BASE_URL}/${id}`);
  }

  createSoftware(data: any): Observable<any> {
    return this.http.post(BASE_URL, data, { headers: this.getAuthHeaders() });
  }

  updateSoftware(id: number, data: any): Observable<any> {
    return this.http.put(`${BASE_URL}/${id}`, data, { headers: this.getAuthHeaders() });
  }

  deleteSoftware(id: number): Observable<any> {
    return this.http.delete(`${BASE_URL}/${id}`, { headers: this.getAuthHeaders() });
  }

  private getAuthHeaders(): HttpHeaders {
    const auth = this.loginService.auth || localStorage.getItem('auth') || '';
    return new HttpHeaders({
        'Authorization': auth,
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
    });
  }
}