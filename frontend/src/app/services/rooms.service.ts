import { Injectable } from '@angular/core';
import { HttpClient,HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { RoomDTO } from '../dtos/room.dto';
import { LoginService } from '../login/login.service';


const BASE_URL = '/api/rooms';

@Injectable({
  providedIn: 'root'
})
export class RoomsService {

  constructor(private http: HttpClient, private loginService: LoginService) { }

  public getRooms(): Observable<RoomDTO[]> {
    //? for the software to load along with the rooms
    return this.http.get<any>(`${BASE_URL}?projection=withSoftware`).pipe(
      map(response => {
        if (response._embedded && response._embedded.rooms) {
          return response._embedded.rooms;
        }
        return response;
      })
    );
  }

  
  public getRoom(id: number | string): Observable<RoomDTO> {
    return this.http.get<RoomDTO>(`${BASE_URL}/${id}?projection=withSoftware`);
  }

  public createRoom(room: any): Observable<any> {
    
    //for the autohentication
    const auth = this.loginService.auth || localStorage.getItem('auth') || '';

    
    const headers = new HttpHeaders({
        'Authorization': auth,
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
    });

    //we call the backend to create the room
    return this.http.post(BASE_URL, room, { headers });
  }

  public updateRoom(id: number, roomData: any): Observable<any> {
    const auth = this.loginService.auth || localStorage.getItem('auth') || '';
    const headers = new HttpHeaders({
        'Authorization': auth,
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
    });

    return this.http.put(`${BASE_URL}/${id}`, roomData, { headers });
  }

  public deleteRoom(id: number): Observable<any> {
    //in case there was no admin, only admin can delete rooms
    const auth = this.loginService.auth || localStorage.getItem('auth') || '';
    
    const headers = new HttpHeaders({
        'Authorization': auth,
        'X-Requested-With': 'XMLHttpRequest'
    });

    return this.http.delete(`${BASE_URL}/${id}`, { headers });
  }
}