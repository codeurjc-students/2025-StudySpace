import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginService } from '../login/login.service';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  constructor(private http: HttpClient, private loginService: LoginService) { }

  private getHeaders() {
    const auth = this.loginService.auth || localStorage.getItem('auth') || '';
    return new HttpHeaders({
      'Authorization': auth,
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    });
  }
  getReservationsByUser(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/users/${userId}/reservations`, { headers: this.getHeaders() });
  }

  
  deleteReservation(id: number): Observable<any> {
    return this.http.delete(`/api/reservations/${id}`, { headers: this.getHeaders() });
  }

  
  updateReservation(id: number, reservation: any): Observable<any> {
    return this.http.put(`/api/reservations/${id}`, reservation, { headers: this.getHeaders() });
  }

  

  createReservation(roomId: number, startDate: Date, endDate: Date, reason: string): Observable<any> {
    const body = {
      roomId: roomId,
      startDate: startDate,
      endDate: endDate,
      reason: reason
    };
    
    return this.http.post('/api/reservations/create', body, { headers: this.getHeaders() });
  }
  
}