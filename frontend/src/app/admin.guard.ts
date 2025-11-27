import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginService } from './login/login.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const loginService = inject(LoginService);
  const router = inject(Router);

  
  if (loginService.isAdmin()) {
    return true; 
  } else {
    alert("⛔ ACCESS DENIED⛔\n\nYou do not have administrator permissions to access this section.");
    return router.createUrlTree(['/']); 
  }
};
