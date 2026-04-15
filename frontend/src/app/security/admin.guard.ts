import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginService } from '../login/login.service';
import { DialogService } from '../services/dialog.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const loginService = inject(LoginService);
  const router = inject(Router);
  const dialogService = inject(DialogService);

  if (loginService.isAdmin()) {
    return true;
  } else {
    dialogService.alert(
      '⛔ ACCESS DENIED ⛔',
      'You do not have administrator permissions to access this section.',
    );
    return router.createUrlTree(['/']);
  }
};
