import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginService } from '../login/login.service';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  constructor(private http: HttpClient, private loginService: LoginService) { }

  createReservation(roomId: number, startDate: Date, endDate: Date, reason: string): Observable<any> {
    const body = {
      roomId: roomId,
      startDate: startDate,
      endDate: endDate,
      reason: reason
    };
    const authHeader = this.loginService.auth || localStorage.getItem('auth') || '';
    const headers = new HttpHeaders({
      'Authorization': authHeader,
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest' // This prevents Spring from displaying the browser login pop-up
    });
    //post request with auth header
    return this.http.post('/api/reservations/create', body);
  }
}