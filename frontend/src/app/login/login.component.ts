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

  //put the loginService in the constructor, router and modalService too
  constructor(
    public loginService: LoginService, 
    private modalService: NgbModal,
    private router: Router
  ) {}

  logIn(user: string, pass: string) {
    this.loginService.logIn(user, pass).subscribe({
      next: (response: any) => {
        console.log("Login correcto:", response);
        // Optional: Redirect to home or another page after successful login
        // this.router.navigate(['/home']);
      },
      error: (error: any) => {
        console.error("Error en login:", error);
      
        this.modalService.open(this.loginErrorModal);
      }
    });
  }

  logOut() {
    this.loginService.logOut();
  }
}
