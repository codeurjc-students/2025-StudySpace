import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, of } from 'rxjs';
import { UserDTO } from '../dtos/user.dto'; 
import { Router } from '@angular/router';

const BASE_URL = '/api/auth';

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  
  //BehaviorSubject to update also components when user logs in/out
  private currentUserSubject = new BehaviorSubject<UserDTO | null>(null);
  
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) { 
    this.checkAuth();
  }

  //Check if there's an active session by requesting current user data
  private checkAuth() {
    this.http.get<UserDTO>(`${BASE_URL}/me`).subscribe({
      next: (user) => {
        console.log("Active session found:", user.name);
        this.currentUserSubject.next(user);
      },
      error: () => {
        //401 if no cookie/session
        this.currentUserSubject.next(null);
      }
    });
  }

  //LOGIN
  logIn(username: string, pass: string): Observable<any> {
    return this.http.post<any>(`${BASE_URL}/login`, { username, password: pass }).pipe(
      tap(() => {
        //ask for current user data after login
        this.checkAuth(); 
      })
    );
  }

  //LOGOUT
  logOut() {
    this.http.post(`${BASE_URL}/logout`, {}).subscribe({
      next: () => this.finalizeLogout(),
      error: () => this.finalizeLogout()
    });
  }



  //Public methods
  
  //REGISTER
  register(name: string, email: string, pass: string): Observable<any> {
    return this.http.post(`${BASE_URL}/register`, { name, email, password: pass });
  }

  updateProfile(name: string, email: string): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${BASE_URL}/me`, { name, email }).pipe(
        tap(updatedUser => {
            const current = this.currentUserSubject.value;
            if (current) {
                this.currentUserSubject.next({ ...current, ...updatedUser });
            }
        })
    );
  }

  // for *ngIf of HTML
  get currentUser(): UserDTO | null {
    return this.currentUserSubject.value;
  }
  

  set currentUser(user: UserDTO | null) {
      this.currentUserSubject.next(user);
  }

  isLogged(): boolean {
    return this.currentUserSubject.value !== null;
  }

  isAdmin(): boolean {
    return this.currentUserSubject.value?.roles?.includes('ADMIN') || false;
  }
  
  reloadUser(): Observable<UserDTO> {
      return this.http.get<UserDTO>(`${BASE_URL}/me`);
  }

  private finalizeLogout() {
    this.currentUserSubject.next(null);
    this.router.navigate(['/']);
  }
}