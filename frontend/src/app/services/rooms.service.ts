import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { RoomDTO } from '../dtos/room.dto';

// Añadimos el parámetro de proyección a la URL base
const BASE_URL = '/api/rooms';

@Injectable({
  providedIn: 'root'
})
export class RoomsService {

  constructor(private http: HttpClient) { }

  public getRooms(): Observable<RoomDTO[]> {
    // Añadimos ?projection=withSoftware para traer los datos completos
    return this.http.get<any>(`${BASE_URL}?projection=withSoftware`).pipe(
      map(response => {
        if (response._embedded && response._embedded.rooms) {
          return response._embedded.rooms;
        }
        return response;
      })
    );
  }

  // NUEVO: Método para obtener una sola aula por ID
  public getRoom(id: number | string): Observable<RoomDTO> {
    return this.http.get<RoomDTO>(`${BASE_URL}/${id}?projection=withSoftware`);
  }
}