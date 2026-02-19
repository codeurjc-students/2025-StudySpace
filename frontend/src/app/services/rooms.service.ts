import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { RoomDTO } from '../dtos/room.dto';
import { Page } from '../dtos/page.model';
import { RoomCalendarDTO } from '../dtos/calendar-data.dto';


//const BASE_URL = 'https://localhost:8443/api/rooms';
const BASE_URL = '/api/rooms';

@Injectable({
  providedIn: 'root'
})
export class RoomsService {

  constructor(private readonly http: HttpClient) { }

  public getRooms(page: number = 0, size: number = 10): Observable<Page<RoomDTO>> {
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

  /*public deleteRoom(id: number): Observable<any> {
    return this.http.delete(`${BASE_URL}/${id}`);
  }*/

  public getRoomStats(id: number, date: string): Observable<any> {
    return this.http.get<any>(`${BASE_URL}/${id}/stats?date=${date}`);
  }



  uploadRoomImage(id: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${BASE_URL}/${id}/image`, formData);
  }



  public deleteRoom(id: number, reason: string): Observable<any> {
    return this.http.delete(`${BASE_URL}/${id}?reason=${encodeURIComponent(reason)}`);
  }




  getRoomCalendar(roomId: number, start: string, end: string): Observable<RoomCalendarDTO> {
    return this.http.get<RoomCalendarDTO>(`/api/rooms/${roomId}/calendar?start=${start}&end=${end}`);
  }


}