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


  it('should register successfully and redirect to login', () => {
    spyOn(window, 'alert');
    const navigateSpy = spyOn(router, 'navigate');
    
    mockLoginService.register.and.returnValue(of({}));

    component.onRegister('Test User', 'test@test.com', 'StrongPass1!');

    expect(mockLoginService.register).toHaveBeenCalledWith('Test User', 'test@test.com', 'StrongPass1!');
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/successfully/));
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should show specific alert for 409 Conflict (Email Exists)', () => {
    spyOn(window, 'alert');
    spyOn(console, 'error');
    
    //409
    const errorResponse = { status: 409 };
    mockLoginService.register.and.returnValue(throwError(() => errorResponse));

    component.onRegister('User', 'duplicate@test.com', 'StrongPass1!');

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/already registered/));
  });

  it('should show generic error alert for other failures', () => {
    spyOn(window, 'alert');
    spyOn(console, 'error');
    
    const errorResponse = { status: 500 };
    mockLoginService.register.and.returnValue(throwError(() => errorResponse));

    component.onRegister('User', 'error@test.com', 'StrongPass1!');

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/Error registering/));
  });




  it('should NOT call register if email format is invalid', () => {
    spyOn(window, 'alert'); 
    
    component.onRegister('Test User', 'invalid-email', 'StrongPass1!');

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/valid email/));
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });

  it('should NOT call register if fields are empty', () => {
    spyOn(window, 'alert');
    component.onRegister('', '', '');
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/fill in all/));
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });



  it('should NOT register if password is weak (does not match pattern)', () => {
    spyOn(window, 'alert'); 
    const weakPass = 'weakpassword123'; 
    
    component.onRegister('Test User', 'test@test.com', weakPass);

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/Password must contain/));
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });


});