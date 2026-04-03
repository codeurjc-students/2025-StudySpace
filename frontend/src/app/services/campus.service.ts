import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CampusDTO } from '../dtos/campus.dto';

@Injectable({
  providedIn: 'root',
})
export class CampusService {
  private readonly BASE_URL = '/api/campus';

  constructor(private readonly http: HttpClient) {}

  getAllCampus(): Observable<CampusDTO[]> {
    return this.http.get<CampusDTO[]>(this.BASE_URL);
  }

  createCampus(campus: CampusDTO): Observable<CampusDTO> {
    return this.http.post<CampusDTO>(this.BASE_URL, campus);
  }

  updateCampus(id: number, campus: CampusDTO): Observable<CampusDTO> {
    return this.http.put<CampusDTO>(`${this.BASE_URL}/${id}`, campus);
  }

  deleteCampus(id: number): Observable<any> {
    return this.http.delete(`${this.BASE_URL}/${id}`);
  }
}
