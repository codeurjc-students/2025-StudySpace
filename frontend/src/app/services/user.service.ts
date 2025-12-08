import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginService } from '../login/login.service';
import { UserDTO } from '../dtos/user.dto';

@Injectable({ providedIn: 'root' })
export class UserService {

  constructor(private readonly http: HttpClient, private readonly loginService: LoginService) { }

  private getHeaders() {
    const auth = this.loginService.auth || localStorage.getItem('auth') || '';
    return new HttpHeaders({ 'Authorization': auth, 'X-Requested-With': 'XMLHttpRequest' });
  }

  getUsers(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>('/api/users', { headers: this.getHeaders() });
  }

  changeRole(id: number, isAdmin: boolean): Observable<any> {
    const role = isAdmin ? 'ADMIN' : 'USER';
    return this.http.put(`/api/users/${id}/role?role=${role}`, {}, { headers: this.getHeaders() });
  }

  toggleBlock(id: number): Observable<any> {
    return this.http.put(`/api/users/${id}/block`, {}, { headers: this.getHeaders() });
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`/api/users/${id}`, { headers: this.getHeaders() });
  }
}