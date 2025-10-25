import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component'; // Importa el componente de login

const routes: Routes = [
  //{ path: '', redirectTo: '/home', pathMatch: 'full' }, // Redirect to root /home
  { path: 'login', component: LoginComponent }, // Route to LoginComponent
  //{ path: '**', redirectTo: '/home' } // Redirige rutas no encontradas a /home
];

@NgModule({
  imports: [RouterModule.forRoot(routes)], // Configura las rutas principales
  exports: [RouterModule]
})
export class AppRoutingModule {}
