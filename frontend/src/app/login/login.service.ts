import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map, switchMap, catchError, of } from 'rxjs';
import { UserDTO } from '../dtos/user.dto';
import { Router } from '@angular/router';


export interface User {
    name: string;
}

@Injectable({
  providedIn: 'root'
})

export class LoginService {
  currentUser: UserDTO | null = null;
  auth: string = '';

  constructor(private http: HttpClient, private router: Router) { this.auth = localStorage.getItem('auth') || '';
    
    if (this.auth) {
      //If credentials are saved, we request the user data from the backend.
      this.getCurrentUser().subscribe({
        next: (user) => {
            this.currentUser = user;
            console.log("Session restored:", user.name);
        },
        error: () => {
            console.warn("Credentials expired or invalid. Logging out.");
            this.logOut(); //if something goes wrong, logout
        }
      });
    }
  }

  register(name: string, email: string, pass: string): Observable<any> {
    const body = {
        name: name,
        email: email,
        password: pass
    };
    
    return this.http.post('http://localhost:8080/api/auth/register', body);
  }

  logIn(user: string, pass: string): Observable<any> {
    const authHeader = 'Basic ' + btoa(user + ':' + pass);
    const headers = new HttpHeaders({ 
        'Authorization': authHeader,
        'X-Requested-With': 'XMLHttpRequest' 
    });

    return this.http.get<UserDTO>('http://localhost:8080/api/auth/me', { headers }).pipe(
      switchMap(userData => {
          return this.http.get<any>(`http://localhost:8080/api/users/${userData.id}/reservations?projection=withRoom`, { headers }).pipe(
              map(res => {
                 const reservations = res._embedded ? res._embedded.reservations : [];
                 userData.reservations = reservations;
                 return userData;
              })
          );
      }),
      map(userWithReservations => {
        this.currentUser = userWithReservations;
        this.auth = authHeader;
        localStorage.setItem('auth', this.auth);
        return userWithReservations;
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
  //auxiliary methods
  private getCurrentUser(): Observable<UserDTO> {
    const headers = new HttpHeaders({ 
        'Authorization': this.auth,
        'X-Requested-With': 'XMLHttpRequest' 
    });

    return this.http.get<UserDTO>('http://localhost:8080/api/auth/me', { headers }).pipe(
      switchMap(userData => {
          return this.http.get<any>(`http://localhost:8080/api/users/${userData.id}/reservations?projection=withRoom`, { headers }).pipe(
              map(res => {
                 const reservations = res._embedded ? res._embedded.reservations : [];
                 userData.reservations = reservations;
                 return userData;
              }),
              // If the booking upload fails, we return the user without bookings to avoid blocking them.
              catchError(() => of(userData)) 
          );
      })
    );
  }
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