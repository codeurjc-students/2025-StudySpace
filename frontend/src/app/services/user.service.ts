import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDTO } from '../dtos/user.dto';
import { Page } from '../dtos/page.model';

const BASE_URL = '/api/users';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private readonly http: HttpClient) {}

  getUsers(page: number = 0, size: number = 10): Observable<Page<UserDTO>> {
    return this.http.get<Page<UserDTO>>(
      `${BASE_URL}?page=${page}&size=${size}`,
    );
  }

  changeRole(id: number, isAdmin: boolean): Observable<any> {
    const role = isAdmin ? 'ADMIN' : 'USER';
    return this.http.put(`${BASE_URL}/${id}/role?role=${role}`, {});
  }

  toggleBlock(id: number): Observable<any> {
    return this.http.put(`${BASE_URL}/${id}/block`, {});
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${BASE_URL}/${id}`);
  }

  uploadUserImage(id: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${BASE_URL}/${id}/image`, formData);
  }

  searchUsers(
    text?: string,
    blocked?: string,
    role?: string,
    roomName?: string,
    date?: string,
    page: number = 0,
    size: number = 10,
  ): Observable<Page<UserDTO>> {
    let queryParams = [`page=${page}&size=${size}`];
    if (text) queryParams.push(`text=${encodeURIComponent(text)}`);
    if (blocked === 'true' || blocked === 'false')
      queryParams.push(`blocked=${blocked}`);
    if (role) queryParams.push(`role=${role}`);
    if (roomName) queryParams.push(`roomName=${encodeURIComponent(roomName)}`);
    if (date) queryParams.push(`date=${date}`);

    const queryString = `?${queryParams.join('&')}`;
    return this.http.get<Page<UserDTO>>(`/api/search/users${queryString}`);
  }
}
