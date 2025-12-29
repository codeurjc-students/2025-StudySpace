import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '../dtos/page.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  constructor(private readonly http: HttpClient) { }

  getReservationsByUser(userId: number, page: number = 0, size: number = 10): Observable<Page<any>> {
    return this.http.get<Page<any>>(`/api/users/${userId}/reservations?page=${page}&size=${size}`);
  }
  
  deleteReservation(id: number): Observable<any> {
    return this.http.delete(`/api/reservations/${id}`);
  }
  
  updateReservation(id: number, reservation: any): Observable<any> {
    return this.http.put(`/api/reservations/${id}`, reservation);
  }

  createReservation(roomId: number, startDate: Date, endDate: Date, reason: string): Observable<any> {
    const body = {
      roomId: roomId,
      startDate: startDate,
      endDate: endDate,
      reason: reason
    };
    return this.http.post('/api/reservations/create', body);
  }

  getMyReservations(page: number = 0, size: number = 10): Observable<Page<any>> {
    return this.http.get<Page<any>>(`/api/reservations/my-reservations?page=${page}&size=${size}`);
  }

  cancelReservation(id: number): Observable<any> {//uses patch
    return this.http.patch(`/api/reservations/${id}/cancel`, {});
  }
}