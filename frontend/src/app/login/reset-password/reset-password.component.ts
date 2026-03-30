import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LoginService } from '../login.service';
import { DialogService } from '../../services/dialog.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['../login.component.css'],
})
export class ResetPasswordComponent implements OnInit {
  token: string = '';
  password: string = '';
  message: string = '';
  error: string = '';
  isLoading: boolean = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly loginService: LoginService,
    private readonly router: Router,
    private readonly dialogService: DialogService,
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'];

    if (!this.token) {
      this.error = 'Invalid or missing token.';
    }
  }

  onSubmit() {
    if (!this.token || !this.password) return;

    const passwordPattern =
      /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&.])(?=\S+$).{8,}$/;
    if (!passwordPattern.test(this.password)) {
      this.error =
        'Password must have 8+ chars, uppercase, lowercase, number and special char.';
      return;
    }

    this.isLoading = true;
    this.loginService.resetPassword(this.token, this.password).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        this.dialogService.alert(
          'Success',
          'Password successfully updated! Please log in.',
        );
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        this.error =
          err.error?.message ||
          'Failed to reset password. The link may have expired.';
      },
    });
  }
}
