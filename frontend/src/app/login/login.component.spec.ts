import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { LoginService } from './login.service';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { NgbModule, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Router, ActivatedRoute } from '@angular/router';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockLoginService: any;
  let mockModalService: any;
  let router: Router;
  let activatedRoute: ActivatedRoute;

  beforeEach(async () => {
    mockLoginService = {
      isLogged: () => false,
      logIn: jasmine.createSpy('logIn'),
      logOut: jasmine.createSpy('logOut')
    };

    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ LoginComponent ],
      imports: [ 
        FormsModule,
        RouterTestingModule.withRoutes([]), 
        NgbModule 
      ],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: NgbModal, useValue: mockModalService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParams: {} } }
        }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    activatedRoute = TestBed.inject(ActivatedRoute);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('Login Success: should navigate to HOME if NO returnUrl', () => {
    const navigateSpy = spyOn(router, 'navigate');
    mockLoginService.logIn.and.returnValue(of({ success: true }));
    
    activatedRoute.snapshot.queryParams = {};

    component.loginData.user = 'user';
    component.loginData.password = 'pass';

    component.logIn();

    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });

  it('Login Success: should navigate to RETURN URL if present', () => {
    const navigateByUrlSpy = spyOn(router, 'navigateByUrl');
    mockLoginService.logIn.and.returnValue(of({ success: true }));

    activatedRoute.snapshot.queryParams = { returnUrl: '/admin/users' };

    component.loginData.user = 'user';
    component.loginData.password = 'pass';

    component.logIn();

    expect(navigateByUrlSpy).toHaveBeenCalledWith('/admin/users');
  });


  it('Login Error 401 (LOCKED): should show Access Denied alert', () => {
    spyOn(window, 'alert');
    spyOn(console, 'error'); // Silenciar log
    const navigateSpy = spyOn(router, 'navigate');

    const errorResponse = { status: 401, error: { message: 'User account is LOCKED' } };
    mockLoginService.logIn.and.returnValue(throwError(() => errorResponse));

    component.loginData.user = 'lockedUser';
    component.loginData.password = 'pass';

    component.logIn();

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/ACCESS DENIED/));
    expect(mockLoginService.logOut).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });

  it('Login Error 401 (Bad Credentials): should show Login Failed alert', () => {
    spyOn(window, 'alert');
    spyOn(console, 'error');
    const navigateSpy = spyOn(router, 'navigate');

    // 401 
    const errorResponse = { status: 401, error: { message: 'Bad credentials' } };
    mockLoginService.logIn.and.returnValue(throwError(() => errorResponse));

    component.loginData.user = 'user';
    component.loginData.password = 'wrongpass';

    component.logIn();

    expect(mockModalService.open).toHaveBeenCalledWith(component.loginErrorModal);
  });

  it('Login Error Other (e.g. 500): should open Error Modal', () => {
    spyOn(console, 'error');
    
    //500
    const errorResponse = { status: 500, message: 'Server error' };
    mockLoginService.logIn.and.returnValue(throwError(() => errorResponse));

    component.loginData.user = 'user';
    component.loginData.password = 'pass';

    component.logIn();

    // Verify
    expect(mockModalService.open).toHaveBeenCalledWith(component.loginErrorModal);
  });
});