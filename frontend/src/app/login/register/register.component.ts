import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from '../login.service'; 

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerData = { name: '', email: '', password: '' };


  //public passwordVisible: boolean = false;

  constructor(
    private readonly loginService: LoginService,
    private readonly router: Router
  ) {}

  /*togglePasswordVisibility() {
    this.passwordVisible = !this.passwordVisible;
  }*/

  onRegister() {
    const { name, email, password } = this.registerData;
    if (!name || !email || !password) {
        alert('Please fill in all fields.');
        return;
    }

    // Email format with Regex
    //text + @ + text + . + text (at least 2 leters)
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailPattern.test(email)) {
        alert('Please enter a valid email address (e.g., userexample@gmail.com).');
        return; 
    }

    //for the password
    const passwordPattern = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&.])(?=\S+$).{8,}$/;
    if (!passwordPattern.test(password)) {
        alert('Password must contain:\n- At least 8 characters\n- One uppercase letter\n- One lowercase letter\n- One number\n- One special character (@$!%*?&.)');
        return; 
    }

    this.loginService.register(name, email, password).subscribe({
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