import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { LoginService } from '../login.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DialogService } from '../../services/dialog.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let mockLoginService: any;
  let router: Router;
  let dialogServiceSpy: jasmine.SpyObj<DialogService>;

  beforeEach(async () => {
    mockLoginService = {
      register: jasmine.createSpy('register'),
    };
    const dSpy = jasmine.createSpyObj('DialogService', ['alert']);
    dSpy.alert.and.returnValue(Promise.resolve());

    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([]),
        FormsModule,
      ],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: DialogService, useValue: dSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    dialogServiceSpy = TestBed.inject(
      DialogService,
    ) as jasmine.SpyObj<DialogService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should register successfully and redirect to login', async () => {
    const navigateSpy = spyOn(router, 'navigate');

    mockLoginService.register.and.returnValue(of({}));

    component.registerData.name = 'Test User';
    component.registerData.email = 'test@test.com';
    component.registerData.password = 'StrongPass1!';

    component.onRegister();

    await fixture.whenStable();

    expect(mockLoginService.register).toHaveBeenCalledWith(
      'Test User',
      'test@test.com',
      'StrongPass1!',
    );
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Registration Successful',
      jasmine.stringMatching(/successfully/),
    );
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should show specific alert for 409 Conflict (Email Exists)', () => {
    spyOn(console, 'error');

    //409
    const errorResponse = { status: 409 };
    mockLoginService.register.and.returnValue(throwError(() => errorResponse));

    component.registerData.name = 'User';
    component.registerData.email = 'duplicate@test.com';
    component.registerData.password = 'StrongPass1!';

    component.onRegister();

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Email Already Registered',
      jasmine.stringMatching(/already registered/),
    );
  });

  it('should show generic error alert for other failures', () => {
    spyOn(console, 'error');

    const errorResponse = { status: 500 };
    mockLoginService.register.and.returnValue(throwError(() => errorResponse));

    component.registerData.name = 'User';
    component.registerData.email = 'error@test.com';
    component.registerData.password = 'StrongPass1!';

    component.onRegister();

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Error',
      jasmine.stringMatching(/Error registering/),
    );
  });

  it('should NOT call register if email format is invalid', () => {
    component.registerData.name = 'Test User';
    component.registerData.email = 'invalid-email';
    component.registerData.password = 'StrongPass1!';

    component.onRegister();

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Incorrect Info',
      jasmine.stringMatching(/valid email/),
    );
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });

  it('should NOT call register if fields are empty', () => {
    component.registerData.name = '';
    component.registerData.email = '';
    component.registerData.password = '';

    component.onRegister();
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Missing Info',
      jasmine.stringMatching(/fill in all/),
    );
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });

  it('should NOT register if password is weak (does not match pattern)', () => {
    component.registerData.name = 'Test User';
    component.registerData.email = 'test@test.com';
    component.registerData.password = 'weakpassword123';

    component.onRegister();

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Incorrect Info',
      jasmine.stringMatching(/Password must contain/),
    );
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });
});
