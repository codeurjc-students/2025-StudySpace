import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component'; // Importa el componente raíz
import { LoginComponent } from './pages/login/login.component'; // Importa el componente de login

@NgModule({
  
  imports: [
    AppComponent,
    LoginComponent,
    BrowserModule,
    AppRoutingModule // Importa el módulo de rutas
  ],
  providers: [],
  bootstrap: [] // Define el componente raíz
})
export class AppModule {}
