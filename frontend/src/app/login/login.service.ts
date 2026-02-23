import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, of } from 'rxjs';
import { UserDTO } from '../dtos/user.dto';
import { Router } from '@angular/router';
import { catchError, map } from 'rxjs/operators';

const BASE_URL = '/api/auth';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  /*private readonly SESSION_TIME_MINUTES = 20; 
  private sessionWarningTimeout: any;
  private sessionExpiredTimeout: any;*/

  //BehaviorSubject to update also components when user logs in/out
  private currentUserSubject = new BehaviorSubject<UserDTO | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {
    this.checkAuth();
  }

  //Check if there's an active session by requesting current user data
  private checkAuth() {
    if (!localStorage.getItem('is_logged_in')) {
      this.currentUserSubject.next(null);
      return;
    }

    this.http
      .get<UserDTO>(`${BASE_URL}/me`)
      .pipe(
        catchError((error) => {
          localStorage.removeItem('is_logged_in');
          return of(null);
        }),
      )
      .subscribe((user) => {
        if (user) {
          console.log('Active session found:', user.name);
          this.currentUserSubject.next(user);
          //this.startSessionTimers();
        } else {
          this.currentUserSubject.next(null);
        }
      });
  }

  //Check if AuthToken cookie exists
  private hasAuthToken(): boolean {
    return document.cookie
      .split(';')
      .some((c) => c.trim().startsWith('AuthToken='));
  }

  //LOGIN
  logIn(username: string, pass: string): Observable<any> {
    return this.http
      .post<any>(`${BASE_URL}/login`, { username, password: pass })
      .pipe(
        tap(() => {
          //ask for current user data after login
          localStorage.setItem('is_logged_in', 'true');
          this.checkAuth();
        }),
      );
  }

  //LOGOUT
  logOut() {
    this.http.post(`${BASE_URL}/logout`, {}).subscribe({
      next: () => this.finalizeLogout(),
      error: () => this.finalizeLogout(),
    });
  }

  //Public methods

  //REGISTER
  register(name: string, email: string, pass: string): Observable<any> {
    return this.http.post(`${BASE_URL}/register`, {
      name,
      email,
      password: pass,
    });
  }

  updateProfile(name: string, email: string): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${BASE_URL}/me`, { name, email }).pipe(
      tap((updatedUser) => {
        const current = this.currentUserSubject.value;
        if (current) {
          this.currentUserSubject.next({ ...current, ...updatedUser });
        }
      }),
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

  reloadUser(): Observable<UserDTO | null> {
    return this.http.get<UserDTO>(`${BASE_URL}/me`).pipe(
      catchError((error) => {
        return of(null);
      }),
    );
  }

  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    return this.http.post(
      `${BASE_URL}/change-password`,
      { oldPassword, newPassword },
      { withCredentials: true },
    );
  }

  private finalizeLogout() {
    //this.clearTimers();
    localStorage.removeItem('is_logged_in');
    this.currentUserSubject.next(null);
    this.router.navigate(['/']);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${BASE_URL}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${BASE_URL}/reset-password`, { token, newPassword });
  }

  refreshToken(): Observable<any> {
    return this.http.post(`${BASE_URL}/refresh`, {}, { withCredentials: true });
  }

  /*private startSessionTimers() {
    this.clearTimers();

    const warningTimeMs = (this.SESSION_TIME_MINUTES - 5) * 60 * 1000; // notify 5 minutes before expiration
    const expiredTimeMs = this.SESSION_TIME_MINUTES * 60 * 1000; // actual real expiration of token

    this.sessionWarningTimeout = setTimeout(() => {
        alert('⏳ Your session will expire in 5 minutes.');
    }, warningTimeMs);

    this.sessionExpiredTimeout = setTimeout(() => {
        alert('🔒 Your session has expired for security reasons. Redirecting to login...');
        this.finalizeLogout();
    }, expiredTimeMs);
  }

  private clearTimers() {
    if (this.sessionWarningTimeout) clearTimeout(this.sessionWarningTimeout);
    if (this.sessionExpiredTimeout) clearTimeout(this.sessionExpiredTimeout);
  }*/
}
