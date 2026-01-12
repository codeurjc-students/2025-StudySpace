import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '../dtos/page.model';


const BASE_URL = 'https://localhost:8443/api/reservations';


@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  constructor(private readonly http: HttpClient) { }

  getReservationsByUser(userId: number, page: number = 0, size: number = 10): Observable<Page<any>> {
    return this.http.get<Page<any>>(`/api/users/${userId}/reservations?page=${page}&size=${size}`);
  }
  
  deleteReservation(id: number): Observable<any> {
    return this.http.delete(`${BASE_URL}/${id}`);
  }
  
  updateReservation(id: number, reservation: any): Observable<any> {
    return this.http.put(`${BASE_URL}/${id}`, reservation);
  }

  createReservation(roomId: number, startDate: Date, endDate: Date, reason: string): Observable<any> {
    const body = {
      roomId: roomId,
      startDate: startDate,
      endDate: endDate,
      reason: reason
    };
    return this.http.post(`${BASE_URL}`, body);
  }

  getMyReservations(page: number = 0, size: number = 10): Observable<Page<any>> {
    return this.http.get<Page<any>>(`${BASE_URL}/my-reservations?page=${page}&size=${size}`);
  }

  cancelReservation(id: number): Observable<any> {//uses patch
    return this.http.patch(`${BASE_URL}/${id}/cancel`, {});
  }

  checkAvailability(roomId: number, date: string): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_URL}/check-availability?roomId=${roomId}&date=${date}`);
  }

}