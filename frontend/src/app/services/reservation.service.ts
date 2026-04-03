import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '../dtos/page.model';

const BASE_URL = '/api/reservations';

@Injectable({
  providedIn: 'root',
})
export class ReservationService {
  constructor(private readonly http: HttpClient) {}

  getReservationsByUser(
    userId: number,
    page: number = 0,
    size: number = 10,
  ): Observable<Page<any>> {
    return this.http.get<Page<any>>(
      `/api/users/${userId}/reservations?page=${page}&size=${size}`,
    );
  }

  deleteReservation(id: number): Observable<any> {
    return this.http.delete(`${BASE_URL}/${id}`);
  }

  updateReservation(id: number, reservation: any): Observable<any> {
    return this.http.put(`${BASE_URL}/${id}`, reservation);
  }

  createReservation(
    roomId: number,
    startDate: Date,
    endDate: Date,
    reason: string,
  ): Observable<any> {
    const body = {
      roomId: roomId,
      startDate: startDate,
      endDate: endDate,
      reason: reason,
    };
    return this.http.post(`${BASE_URL}`, body);
  }

  getMyReservations(
    page: number = 0,
    size: number = 10,
  ): Observable<Page<any>> {
    return this.http.get<Page<any>>(
      `${BASE_URL}/my-reservations?page=${page}&size=${size}`,
    );
  }

  cancelReservation(id: number): Observable<any> {
    //uses patch
    return this.http.patch(`${BASE_URL}/${id}/cancel`, {});
  }

  checkAvailability(roomId: number, date: string): Observable<any[]> {
    return this.http.get<any[]>(
      `${BASE_URL}/check-availability?roomId=${roomId}&date=${date}`,
    );
  }

  updateReservationAdmin(id: number, data: any): Observable<any> {
    return this.http.put(`${BASE_URL}/admin/${id}`, data);
  }

  cancelReservationAdmin(id: number, reason: string): Observable<any> {
    return this.http.patch(`${BASE_URL}/admin/${id}/cancel`, { reason });
  }

  searchReservationsAdmin(
    userId: number,
    text?: string,
    date?: string,
    page: number = 0,
    size: number = 10,
  ): Observable<Page<any>> {
    let queryParams = [`page=${page}&size=${size}`];
    if (text) queryParams.push(`text=${encodeURIComponent(text)}`);
    if (date) queryParams.push(`date=${date}`);
    return this.http.get<Page<any>>(
      `/api/search/reservations/user/${userId}?${queryParams.join('&')}`,
    );
  }

  searchMyReservations(
    text?: string,
    date?: string,
    page: number = 0,
    size: number = 10,
  ): Observable<Page<any>> {
    let queryParams = [`page=${page}&size=${size}`];
    if (text) queryParams.push(`text=${encodeURIComponent(text)}`);
    if (date) queryParams.push(`date=${date}`);
    return this.http.get<Page<any>>(
      `/api/search/reservations/me?${queryParams.join('&')}`,
    );
  }

  smartSearch(
    start: Date,
    end: Date,
    minCapacity?: number,
    campusId?: number,
  ): Observable<any[]> {
    let url = `${BASE_URL}/smart-search?start=${start.toISOString()}&end=${end.toISOString()}`;

    if (minCapacity) {
      url += `&minCapacity=${minCapacity}`;
    }
    if (campusId) {
      url += `&campusId=${campusId}`;
    }

    return this.http.get<any[]>(url);
  }
}
