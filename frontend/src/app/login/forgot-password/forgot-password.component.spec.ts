import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ForgotPasswordComponent } from './forgot-password.component';
import { LoginService } from '../login.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

describe('ForgotPasswordComponent', () => {
  let component: ForgotPasswordComponent;
  let fixture: ComponentFixture<ForgotPasswordComponent>;
  let loginServiceSpy: jasmine.SpyObj<LoginService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('LoginService', ['forgotPassword']);

    await TestBed.configureTestingModule({
      declarations: [ ForgotPasswordComponent ],
      imports: [ 
        FormsModule,            
        RouterTestingModule,    
        HttpClientTestingModule 
      ],
      providers: [
        { provide: LoginService, useValue: spy }
      ]
    })
    .compileComponents();

    loginServiceSpy = TestBed.inject(LoginService) as jasmine.SpyObj<LoginService>;
    fixture = TestBed.createComponent(ForgotPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not call service if email is empty', () => {
    component.email = '';
    component.onSubmit();
    expect(loginServiceSpy.forgotPassword).not.toHaveBeenCalled();
  });

  it('should set message on successful request', () => {
    const mockResponse = { message: 'Link sent' };
    loginServiceSpy.forgotPassword.and.returnValue(of(mockResponse));

    component.email = 'test@example.com';
    component.onSubmit();

    expect(component.isLoading).toBeFalse();
    expect(component.message).toBe('Link sent');
    expect(component.error).toBe('');
  });

  it('should set error on failed request', () => {
    const mockError = { error: { message: 'User not found' } };
    loginServiceSpy.forgotPassword.and.returnValue(throwError(() => mockError));

    component.email = 'wrong@example.com';
    component.onSubmit();

    expect(component.isLoading).toBeFalse();
    expect(component.message).toBe('');
    expect(component.error).toBe('User not found');
  });
});