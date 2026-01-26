import { Component } from '@angular/core';
import { LoginService } from '../login.service'; 

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  email: string = '';
  message: string = '';
  error: string = '';
  isLoading: boolean = false;

  constructor(private loginService: LoginService) {}

  onSubmit() {
    if (!this.email) return;

    this.isLoading = true;
    this.message = '';
    this.error = '';

    this.loginService.forgotPassword(this.email).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        this.message = res.message || 'If the email exists, a link has been sent.';
      },
      error: (err) => {
        this.isLoading = false;
        this.error = err.error?.message || 'An error occurred. Please try again.';
      }
    });
  }
}