import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { LoginService } from './login.service';
import { UserDTO } from '../dtos/user.dto';

describe('LoginService', () => {
  let service: LoginService;
  let httpMock: HttpTestingController;

  const mockUser: UserDTO = { 
    id: 1, name: 'Test User', email: 'test@test.com', roles: ['USER'], blocked: false, reservations: [] 
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, RouterTestingModule ],
      providers: [ LoginService ]
    });
    
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });


  it('should verify auth on startup if token exists', () => {
    localStorage.setItem('is_logged_in', 'true');
    
    service = TestBed.inject(LoginService);

    const req = httpMock.expectOne('/api/auth/me');
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);

    expect(service.currentUser).toEqual(mockUser);
    expect(service.isLogged()).toBeTrue();
  });

  it('should clear auth if token is invalid on startup', () => {
    localStorage.setItem('is_logged_in', 'true');
    service = TestBed.inject(LoginService); 

    const req = httpMock.expectOne('/api/auth/me');
    req.flush('Invalid token', { status: 401, statusText: 'Unauthorized' });

    expect(service.currentUser).toBeNull();
    expect(localStorage.getItem('is_logged_in')).toBeNull();
  });



  it('logIn should post to API, then checkAuth (GET), and update state', () => {
    service = TestBed.inject(LoginService); 

    service.logIn('user', 'pass').subscribe();

    const reqLogin = httpMock.expectOne('/api/auth/login');
    expect(reqLogin.request.method).toBe('POST');
    reqLogin.flush({}); 

    const reqMe = httpMock.expectOne('/api/auth/me');
    expect(reqMe.request.method).toBe('GET');
    reqMe.flush(mockUser); 

    expect(service.currentUser).toEqual(mockUser);
    expect(localStorage.getItem('is_logged_in')).toBe('true');
  });

  it('logOut should post to API and clear state', () => {
    service = TestBed.inject(LoginService); 
    
    service.currentUser = mockUser;
    localStorage.setItem('is_logged_in', 'true');

    service.logOut();

    const req = httpMock.expectOne('/api/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush({});

    expect(service.currentUser).toBeNull();
    expect(localStorage.getItem('is_logged_in')).toBeNull();
  });



  it('updateProfile should put to API and update local observable', () => {
    service = TestBed.inject(LoginService);
    service.currentUser = mockUser;
    const updatedData = { ...mockUser, name: 'New Name' };

    service.updateProfile('New Name', 'test@test.com').subscribe();

    const req = httpMock.expectOne('/api/auth/me');
    expect(req.request.method).toBe('PUT');
    req.flush(updatedData);

    expect(service.currentUser?.name).toBe('New Name');
  });

  it('isAdmin should return correct boolean', () => {
    service = TestBed.inject(LoginService);
    
    service.currentUser = { ...mockUser, roles: ['USER', 'ADMIN'] };
    expect(service.isAdmin()).toBeTrue();

    service.currentUser = { ...mockUser, roles: ['USER'] };
    expect(service.isAdmin()).toBeFalse();
  });

  it('register should post data to API', () => {
    service = TestBed.inject(LoginService);
    service.register('New User', 'new@test.com', '1234').subscribe();

    const req = httpMock.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('changePassword should post data to API', () => {
    service = TestBed.inject(LoginService);
    service.changePassword('oldPass', 'newPass').subscribe();

    const req = httpMock.expectOne('/api/auth/change-password');
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('reloadUser should return user on success', () => {
    service = TestBed.inject(LoginService);
    service.reloadUser().subscribe(user => {
        expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne('/api/auth/me');
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  it('reloadUser should return null on error', () => {
    service = TestBed.inject(LoginService);
    service.reloadUser().subscribe(user => {
        expect(user).toBeNull();
    });

    const req = httpMock.expectOne('/api/auth/me');
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });
});