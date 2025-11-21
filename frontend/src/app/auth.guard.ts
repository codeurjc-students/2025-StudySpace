import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginService } from './login/login.service';

export const authGuard: CanActivateFn = (route, state) => {
  const loginService = inject(LoginService);
  const router = inject(Router);

  if (loginService.isLogged()) {
    return true; //if logged in, proceed to the requested route
  } else {
    // if not logged in, redirect to login page
    return router.createUrlTree(['/login']);
  }
};