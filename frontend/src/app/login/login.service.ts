import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface User {
    name: string;
}

@Injectable({
  providedIn: 'root'
})
// ¡ASEGÚRATE DE QUE TENGA LA PALABRA 'export' AQUÍ!
export class LoginService {
  currentUser: User | null = null;
  auth: string = '';

  constructor(private http: HttpClient) { }

  logIn(user: string, pass: string): Observable<any> {
    const authHeader = 'Basic ' + btoa(user + ':' + pass);
    const headers = new HttpHeaders({ 
        'Authorization': authHeader,
        'X-Requested-With': 'XMLHttpRequest' 
    });

    return this.http.get('http://localhost:8080/api/softwares', { headers }).pipe(
      map(response => {
        this.currentUser = { name: user };
        this.auth = authHeader;
        localStorage.setItem('auth', this.auth);
        return response;
      })
    );
  }

  logOut() {
    this.currentUser = null;
    this.auth = '';
    localStorage.removeItem('auth');
  }

  isLogged(): boolean {
    return this.currentUser !== null;
  }
}