import { Component, ViewChild, TemplateRef } from '@angular/core';
import { LoginService } from './login.service'; // Asegúrate que la ruta sea correcta
import { NgbModal } from '@ng-bootstrap/ng-bootstrap'; // Necesario para tu modal de error
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
@ViewChild('loginErrorModal') loginErrorModal!: TemplateRef<any>;

  // Inyectamos el servicio de Login, el de Modales y el Router
  constructor(
    public loginService: LoginService, 
    private modalService: NgbModal,
    private router: Router
  ) {}

  logIn(user: string, pass: string) {
    this.loginService.logIn(user, pass).subscribe({
      next: (response) => {
        console.log("Login correcto:", response);
        // Opcional: Redirigir a otra página al loguear
        // this.router.navigate(['/home']);
      },
      error: (error) => {
        console.error("Error en login:", error);
        // Abrir el modal de error que tienes en el HTML
        this.modalService.open(this.loginErrorModal);
      }
    });
  }

  logOut() {
    this.loginService.logOut();
  }
}
