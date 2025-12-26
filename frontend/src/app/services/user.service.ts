import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDTO } from '../dtos/user.dto';
import { Page } from '../dtos/page.model';

@Injectable({ providedIn: 'root' })
export class UserService {

  constructor(private readonly http: HttpClient) { }


  getUsers(page: number = 0, size: number = 10): Observable<Page<UserDTO>> {
    return this.http.get<Page<UserDTO>>(`/api/users?page=${page}&size=${size}`);
  }

  changeRole(id: number, isAdmin: boolean): Observable<any> {
    const role = isAdmin ? 'ADMIN' : 'USER';
    return this.http.put(`/api/users/${id}/role?role=${role}`, {});
  }

  toggleBlock(id: number): Observable<any> {
    return this.http.put(`/api/users/${id}/block`, {});
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`/api/users/${id}`);
  }
}