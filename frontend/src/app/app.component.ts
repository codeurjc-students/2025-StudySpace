import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';
  constructor(private router: Router) {}
  goToLogIn() {
    console.log('Navegando a login...');
    
    // 4. Usamos el router para navegar a la ruta '/login'
    // (Ref: Tema 4, página 16)
    this.router.navigate(['/login']);
  }
}
