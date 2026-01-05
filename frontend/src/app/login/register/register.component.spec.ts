import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { LoginService } from '../login.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let mockLoginService: any;
  let router: Router;

  beforeEach(async () => {
    mockLoginService = {
      register: jasmine.createSpy('register')
    };

    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [
        HttpClientTestingModule, 
        RouterTestingModule.withRoutes([]),    
        FormsModule             
      ],
      providers: [
        { provide: LoginService, useValue: mockLoginService }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // --- TESTS DE REGISTRO ---

  it('should register successfully and redirect to login', () => {
    spyOn(window, 'alert');
    const navigateSpy = spyOn(router, 'navigate');
    
    // Simulamos respuesta exitosa
    mockLoginService.register.and.returnValue(of({}));

    component.onRegister('Test User', 'test@test.com', '1234');

    expect(mockLoginService.register).toHaveBeenCalledWith('Test User', 'test@test.com', '1234');
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/successfully/));
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should show specific alert for 409 Conflict (Email Exists)', () => {
    spyOn(window, 'alert');
    spyOn(console, 'error'); // Silenciar log de error
    
    // Simulamos error 409
    const errorResponse = { status: 409 };
    mockLoginService.register.and.returnValue(throwError(() => errorResponse));

    component.onRegister('User', 'duplicate@test.com', '1234');

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/already registered/));
  });

  it('should show generic error alert for other failures', () => {
    spyOn(window, 'alert');
    spyOn(console, 'error');
    
    // Simulamos error 500
    const errorResponse = { status: 500 };
    mockLoginService.register.and.returnValue(throwError(() => errorResponse));

    component.onRegister('User', 'error@test.com', '1234');

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/Error registering/));
  });
});