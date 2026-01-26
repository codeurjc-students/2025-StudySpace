import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LoginService } from '../login.service'; 

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  token: string = '';
  password: string = '';
  message: string = '';
  error: string = '';
  isLoading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private loginService: LoginService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'];
    
    if (!this.token) {
        this.error = 'Invalid or missing token.';
    }
  }

  onSubmit() {
    if (!this.token || !this.password) return;

    const passwordPattern = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&.])(?=\S+$).{8,}$/;
    if (!passwordPattern.test(this.password)) {
        this.error = 'Password must have 8+ chars, uppercase, lowercase, number and special char.';
        return;
    }

    this.isLoading = true;
    this.loginService.resetPassword(this.token, this.password).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        alert('Password successfully updated! Please log in.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        this.error = err.error?.message || 'Failed to reset password. The link may have expired.';
      }
    });
  }
}