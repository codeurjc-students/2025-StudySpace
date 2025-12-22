import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { RoomDTO } from '../dtos/room.dto';
import { Page } from '../dtos/page.model';

const BASE_URL = '/api/rooms';

@Injectable({
  providedIn: 'root'
})
export class RoomsService {

  constructor(private readonly http: HttpClient) { }

  public getRooms(page: number = 0, size: number = 10): Observable<Page<RoomDTO>> {
    /*return this.http.get<any>(`${BASE_URL}?projection=withSoftware`).pipe(
      map(response => {
        if (response._embedded?.rooms) {
          return response._embedded.rooms;
        }
        return response;
      })
    );*/
    return this.http.get<Page<RoomDTO>>(`${BASE_URL}?page=${page}&size=${size}`);
  }
  
  public getRoom(id: number | string): Observable<RoomDTO> {
    return this.http.get<RoomDTO>(`${BASE_URL}/${id}?projection=withSoftware`);
  }

  public createRoom(room: any): Observable<any> {
    // Sin auth headers manuales
    return this.http.post(BASE_URL, room);
  }

  public updateRoom(id: number, roomData: any): Observable<any> {
    return this.http.put(`${BASE_URL}/${id}`, roomData);
  }

  public deleteRoom(id: number): Observable<any> {
    return this.http.delete(`${BASE_URL}/${id}`);
  }

  public getRoomStats(id: number, date: string): Observable<any> {
    return this.http.get<any>(`${BASE_URL}/${id}/stats?date=${date}`);
  }
}