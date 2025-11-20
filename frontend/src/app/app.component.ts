import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from './login/login.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';
  constructor(public loginService: LoginService,private router: Router) {}
  goToLogIn() {
    console.log('Navegando a login...');
    this.router.navigate(['/login']);
  }
}
