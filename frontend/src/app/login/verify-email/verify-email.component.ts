import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.css'],
})
export class VerifyEmailComponent implements OnInit {
  loading = true;
  success = false;
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly http: HttpClient,
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (token) {
      this.verifyToken(token);
    } else {
      this.handleError('No verification token provided in the link.');
    }
  }

  verifyToken(token: string) {
    this.http
      .get<any>('/api/auth/verify-email', {
        params: { token: token },
      })
      .subscribe({
        next: (res) => {
          this.loading = false;
          this.success = true;
        },
        error: (err) => {
          const msg =
            err.error?.message ||
            err.error ||
            'The verification link is invalid or has expired.';
          this.handleError(msg);
        },
      });
  }

  private handleError(message: string) {
    this.loading = false;
    this.success = false;
    this.errorMessage = message;
  }
}
