import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResetPasswordComponent } from './reset-password.component';
import { LoginService } from '../login.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { DialogService } from '../../services/dialog.service';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let loginServiceSpy: jasmine.SpyObj<LoginService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let dialogServiceSpy: jasmine.SpyObj<DialogService>;

  const activatedRouteMock = {
    snapshot: {
      queryParams: { token: 'valid-token-123' },
    },
  };

  beforeEach(async () => {
    const lSpy = jasmine.createSpyObj('LoginService', ['resetPassword']);
    const rSpy = jasmine.createSpyObj('Router', ['navigate']);
    const dSpy = jasmine.createSpyObj('DialogService', ['alert']);
    dSpy.alert.and.returnValue(Promise.resolve());

    await TestBed.configureTestingModule({
      declarations: [ResetPasswordComponent],
      imports: [FormsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        { provide: LoginService, useValue: lSpy },
        { provide: Router, useValue: rSpy },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: DialogService, useValue: dSpy },
      ],
    }).compileComponents();

    loginServiceSpy = TestBed.inject(
      LoginService,
    ) as jasmine.SpyObj<LoginService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    dialogServiceSpy = TestBed.inject(
      DialogService,
    ) as jasmine.SpyObj<DialogService>;

    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and capture token from URL', () => {
    expect(component).toBeTruthy();
    expect(component.token).toBe('valid-token-123');
    expect(component.error).toBe('');
  });

  it('should validate weak passwords frontend-side', () => {
    component.password = 'weak';
    component.onSubmit();

    expect(loginServiceSpy.resetPassword).not.toHaveBeenCalled();
    expect(component.error).toContain('Password must have 8+ chars');
  });

  it('should call resetPassword and navigate on success', () => {
    loginServiceSpy.resetPassword.and.returnValue(of({ message: 'Success' }));

    component.password = 'StrongPass1!';
    component.onSubmit();

    expect(loginServiceSpy.resetPassword).toHaveBeenCalledWith(
      'valid-token-123',
      'StrongPass1!',
    );

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Success',
      'Password successfully updated! Please log in.',
    );
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should handle service errors (expired token)', () => {
    const mockError = { error: { message: 'Token expired' } };
    loginServiceSpy.resetPassword.and.returnValue(throwError(() => mockError));

    component.password = 'StrongPass1!';
    component.onSubmit();

    expect(component.isLoading).toBeFalse();
    expect(component.error).toBe('Token expired');
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });
});
