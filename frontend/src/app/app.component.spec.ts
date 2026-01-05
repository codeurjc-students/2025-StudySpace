import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AppComponent } from './app.component';
import { LoginService } from './login/login.service';
import { Router } from '@angular/router';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientTestingModule // Para que LoginService funcione (simula backend)
      ],
      declarations: [
        AppComponent
      ],
      providers: [
        LoginService 
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router); // Inyectamos el router para espiarlo
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it(`should have as title 'frontend'`, () => {
    expect(component.title).toEqual('frontend');
  });

  // --- NUEVO TEST PARA goToLogIn ---
  
  it('goToLogIn should log message and navigate to /login', () => {
    // 1. Espiamos el método navigate del router
    const navigateSpy = spyOn(router, 'navigate');
    
    // 2. Espiamos console.log para verificar que se llama (y evitar ruido en la terminal)
    spyOn(console, 'log');

    // 3. Ejecutamos el método
    component.goToLogIn();

    // 4. Verificaciones
    expect(console.log).toHaveBeenCalledWith('Navegando a login...');
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});