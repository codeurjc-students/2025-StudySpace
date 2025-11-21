import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { UserDTO } from '../dtos/user.dto';
import { Router } from '@angular/router';

export interface User {
    name: string;
}

@Injectable({
  providedIn: 'root'
})
// ¡ASEGÚRATE DE QUE TENGA LA PALABRA 'export' AQUÍ!
export class LoginService {
  currentUser: UserDTO | null = null;
  auth: string = '';

  constructor(private http: HttpClient, private router: Router) { }

  register(name: string, email: string, pass: string): Observable<any> {
    const body = {
        name: name,
        email: email,
        password: pass
    };
    // Hacemos un POST al endpoint que acabamos de crear en Java
    return this.http.post('http://localhost:8080/api/auth/register', body);
  }

  logIn(user: string, pass: string): Observable<any> {
    const authHeader = 'Basic ' + btoa(user + ':' + pass);
    const headers = new HttpHeaders({ 
        'Authorization': authHeader,
        'X-Requested-With': 'XMLHttpRequest' 
    });

    return this.http.get('http://localhost:8080/api/auth/me', { headers }).pipe(
      map(response => {
        this.currentUser = response as UserDTO;
        this.auth = authHeader;
        localStorage.setItem('auth', this.auth);
        return response;
      })
    );
  }

  logOut() {
    this.http.post('http://localhost:8080/api/auth/logout', {}).subscribe({
        next: (response) => {
            console.log("Successful logout");
            this.finalizeLogout();
        },
        error: (error) => {
            console.warn("Error in backend logout", error);
            this.finalizeLogout();
        }
    });
  }
  //auxiliary method
  private finalizeLogout() {
      this.currentUser = null;
      this.auth = '';
      localStorage.removeItem('auth');
      
      //back to home
      this.router.navigate(['/']); 
  }

  isLogged(): boolean {
    return this.currentUser !== null;
  }
  isAdmin(): boolean {
      return this.currentUser?.roles.includes('ADMIN') || false;
  }


}