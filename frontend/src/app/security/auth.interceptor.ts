// src/app/security/auth.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor() {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    /*
    // 1. Intentamos leer la cookie 'AuthToken'
    const token = this.getCookie('AuthToken');

    if (token) {
      // 2. Si existe, clonamos la petición y añadimos la cabecera Authorization
      const authReq = request.clone({
        headers: request.headers.set('Authorization', `Bearer ${token}`)
      });
      return next.handle(authReq);
    }

    // 3. Si no hay cookie, pasa la petición normal (ej. login)
    return next.handle(request);
  }

  // Función auxiliar para leer cookies nativas del navegador
  private getCookie(name: string): string | null {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
    return null;
  }*/


  const authReq = request.clone({
      withCredentials: true 
    });

    
    return next.handle(authReq);
  }
}
