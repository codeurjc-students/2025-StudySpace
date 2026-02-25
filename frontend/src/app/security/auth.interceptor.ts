import { Injectable, Injector } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, switchMap, filter, take } from 'rxjs/operators';
import { LoginService } from '../login/login.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(
    null,
  );

  //injector to avoid circular depndency with login service
  constructor(private injector: Injector) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler,
  ): Observable<HttpEvent<unknown>> {
    //alwais cookies to back
    request = request.clone({ withCredentials: true });

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // 401 and no try to login
        if (
          error.status === 401 &&
          !request.url.includes('/api/auth/login') &&
          !request.url.includes('/api/auth/refresh') &&
          !request.url.includes('/api/auth/logout')
        ) {
          return this.handle401Error(request, next);
        }
        return throwError(() => error);
      }),
    );
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler) {
    const loginService = this.injector.get(LoginService);

    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      //ask new token
      return loginService.refreshToken().pipe(
        switchMap(() => {
          this.isRefreshing = false;
          this.refreshTokenSubject.next(true);

          //clone oringinal to add token on petition
          return next.handle(request.clone({ withCredentials: true }));
        }),
        catchError((err) => {
          this.isRefreshing = false;
          console.error('Failure when refreshing the token:', err);

          alert('🔒Your session has permanently expired. Please log in again.');
          loginService.logOut();
          return throwError(() => err);
        }),
      );
    } else {
      //already a resfesh we wait
      return this.refreshTokenSubject.pipe(
        filter((token) => token != null),
        take(1),
        switchMap(() => {
          return next.handle(request.clone({ withCredentials: true }));
        }),
      );
    }
  }
}
