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
        alert('Usuario registrado con éxito. Ahora puedes iniciar sesión.');
        this.router.navigate(['/login']); // Redirigir al login tras registrarse
      },
      error: (err) => {
        console.error(err);
        if (err.status === 409) {
            alert('Ese email ya está registrado.');
        } else {
            alert('Error al registrar usuario. Inténtalo de nuevo.');
        }
      }
    });
  }
}