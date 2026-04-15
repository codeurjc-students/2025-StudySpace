import { Component, ViewChild, TemplateRef } from '@angular/core';
import { LoginService } from './login.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Router, ActivatedRoute } from '@angular/router';
import { DialogService } from '../services/dialog.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  @ViewChild('loginErrorModal') loginErrorModal!: TemplateRef<any>;
  loginData = { user: '', password: '' };

  //put the loginService in the constructor, router and modalService too
  constructor(
    public loginService: LoginService,
    private readonly modalService: NgbModal,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly dialogService: DialogService,
  ) {}

  logIn() {
    this.loginService
      .logIn(this.loginData.user, this.loginData.password)
      .subscribe({
        next: (response: any) => {
          console.log('Correct login:', response);
          const returnUrl = this.route.snapshot.queryParams['returnUrl'];
          if (returnUrl) {
            this.router.navigateByUrl(returnUrl);
          } else {
            this.router.navigate(['/']);
          }
        },

        error: (err: any) => {
          console.error('Login error:', err);

          if (err.status === 401 || err.status === 403) {
            const errorMsg =
              err.error?.message || err.error?.error || err.message || '';
            const lowerMsg = errorMsg.toLowerCase();

            if (lowerMsg.includes('locked')) {
              this.dialogService
                .alert(
                  '⛔ ACCESS DENIED ⛔',
                  'Your account has been LOCKED by an administrator.\nContact support.',
                )
                .then(() => {
                  this.loginService.logOut();
                  this.router.navigate(['/']);
                });
            } else if (
              lowerMsg.includes('verify') ||
              lowerMsg.includes('disabled')
            ) {
              this.dialogService.alert(
                'Verification Required',
                'You must verify your email address before logging in. Please check your inbox for the activation link.',
              );
            } else {
              this.dialogService.alert(
                'Login Error',
                'Incorrect username or password. Please try again.',
              );
            }
          } else {
            this.dialogService.alert(
              'Login Error',
              'A server error occurred. Please try again later.',
            );
          }
        },
      });
  }

  logOut() {
    this.loginService.logOut();
  }
}
