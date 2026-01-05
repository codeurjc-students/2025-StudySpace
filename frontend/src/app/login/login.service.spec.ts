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
    
    // IMPORTANTE: No inyectamos LoginService aquí para poder configurar localStorage antes del constructor en los tests
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  // --- 1. TEST INICIALIZACIÓN (Constructor) ---

  it('should verify auth on startup if token exists', () => {
    // 1. Configuramos el entorno ANTES de crear el servicio
    localStorage.setItem('is_logged_in', 'true');
    
    // 2. Al inyectar, se dispara el constructor -> checkAuth() -> GET
    service = TestBed.inject(LoginService);

    // 3. Esperamos la petición automática del constructor
    const req = httpMock.expectOne('/api/auth/me');
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);

    expect(service.currentUser).toEqual(mockUser);
    expect(service.isLogged()).toBeTrue();
  });

  it('should clear auth if token is invalid on startup', () => {
    localStorage.setItem('is_logged_in', 'true');
    service = TestBed.inject(LoginService); // Constructor dispara GET

    const req = httpMock.expectOne('/api/auth/me');
    req.flush('Invalid token', { status: 401, statusText: 'Unauthorized' });

    expect(service.currentUser).toBeNull();
    expect(localStorage.getItem('is_logged_in')).toBeNull();
  });

  // --- 2. LOGIN & LOGOUT ---

  it('logIn should post to API, then checkAuth (GET), and update state', () => {
    service = TestBed.inject(LoginService); // Constructor vacío (sin token)

    service.logIn('user', 'pass').subscribe();

    // 1. Esperamos el POST del login
    const reqLogin = httpMock.expectOne('/api/auth/login');
    expect(reqLogin.request.method).toBe('POST');
    reqLogin.flush({}); // Respondemos al login

    // 2. El tap() del login llama a checkAuth(), que dispara un GET. Debemos gestionarlo.
    const reqMe = httpMock.expectOne('/api/auth/me');
    expect(reqMe.request.method).toBe('GET');
    reqMe.flush(mockUser); // Respondemos con el usuario

    expect(service.currentUser).toEqual(mockUser);
    expect(localStorage.getItem('is_logged_in')).toBe('true');
  });

  it('logOut should post to API and clear state', () => {
    service = TestBed.inject(LoginService); 
    
    // Simulamos estado logueado
    service.currentUser = mockUser;
    localStorage.setItem('is_logged_in', 'true');

    service.logOut();

    const req = httpMock.expectOne('/api/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush({});

    expect(service.currentUser).toBeNull();
    expect(localStorage.getItem('is_logged_in')).toBeNull();
  });

  // --- 3. OTROS MÉTODOS ---

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