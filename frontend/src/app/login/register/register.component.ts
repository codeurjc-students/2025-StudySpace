import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from '../login.service'; 

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  constructor(
    private loginService: LoginService,
    private router: Router
  ) {}

  onRegister(name: string, email: string, pass: string) {
    this.loginService.register(name, email, pass).subscribe({
      next: (response) => {
        alert('User successfully registered. You can now log in.');
        this.router.navigate(['/login']); // Redirect to login page
      },
      error: (err) => {
        console.error(err);
        if (err.status === 409) {
            alert('That email is already registered. Please use a different email.');
        } else {
            alert('Error registering user. Please try again.');
        }
      }
    });
  }
}