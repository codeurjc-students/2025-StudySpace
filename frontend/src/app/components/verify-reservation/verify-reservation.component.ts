import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-verify-reservation',
  templateUrl: './verify-reservation.component.html',
  styleUrls: ['./verify-reservation.component.css']
})
export class VerifyReservationComponent implements OnInit {
  loading = true;
  success = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
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
    this.http.get('/api/reservations/verify', { 
        params: { token: token },
        responseType: 'text' 
    })
    .subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
      },
      error: (err) => {
        const msg = err.error || 'The verification link is invalid or has expired.';
        this.handleError(msg);
      }
    });
  }

  private handleError(message: string) {
    this.loading = false;
    this.success = false;
    this.errorMessage = message;
  }
}