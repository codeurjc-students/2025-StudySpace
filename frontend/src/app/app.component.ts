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
  constructor(public loginService: LoginService,private readonly router: Router) {}
  goToLogIn() {
    console.log('Navegando a login...');
    this.router.navigate(['/login']);
  }
  getUserImageUrl(): string {
    const user = this.loginService.currentUser;
    if (user && user.imageName) {
      return `https://localhost:8443/api/users/${user.id}/image`;
    }
    
    return 'assets/user_placeholder.png';
  }
}
