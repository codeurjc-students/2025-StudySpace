import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from '../login.service';
import { DialogService } from '../../services/dialog.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['../login.component.css'],
})
export class RegisterComponent {
  registerData = { name: '', email: '', password: '' };

  constructor(
    private readonly loginService: LoginService,
    private readonly router: Router,
    private readonly dialogService: DialogService,
  ) {}

  onRegister() {
    const { name, email, password } = this.registerData;
    if (!name || !email || !password) {
      this.dialogService.alert('Missing Info', 'Please fill in all fields.');
      return;
    }

    // Email format with Regex
    //text + @ + text + . + text (at least 2 leters)
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailPattern.test(email)) {
      this.dialogService.alert(
        'Incorrect Info',
        'Please enter a valid email address (e.g., userexample@gmail.com).',
      );
      return;
    }

    //for the password
    const passwordPattern =
      /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&.])(?=\S+$).{8,}$/;
    if (!passwordPattern.test(password)) {
      this.dialogService.alert(
        'Incorrect Info',
        'Password must contain:\n- At least 8 characters\n- One uppercase letter\n- One lowercase letter\n- One number\n- One special character (@$!%*?&.)',
      );
      return;
    }

    this.loginService.register(name, email, password).subscribe({
      next: (response) => {
        this.dialogService.alert(
          'Success',
          'User successfully registered. You can now log in.',
        );
        this.router.navigate(['/login']); // Redirect to login page
      },
      error: (err) => {
        console.error(err);
        if (err.status === 409) {
          this.dialogService.alert(
            'Email Already Registered',
            'That email is already registered. Please use a different email.',
          );
        } else {
          this.dialogService.alert(
            'Error',
            'Error registering user. Please try again.',
          );
        }
      },
    });
  }
}
