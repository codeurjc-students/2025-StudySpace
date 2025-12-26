import { TestBed } from '@angular/core/testing';
import { CanActivateFn,Router, UrlTree } from '@angular/router';
import { LoginService } from '../login/login.service';

import { adminGuard } from './admin.guard';

describe('adminGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => adminGuard(...guardParameters));

  let loginServiceSpy: jasmine.SpyObj<LoginService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
   //create the mocks
    loginServiceSpy = jasmine.createSpyObj('LoginService', ['isAdmin']);
    routerSpy = jasmine.createSpyObj('Router', ['createUrlTree']);

    TestBed.configureTestingModule({
      providers: [
        { provide: LoginService, useValue: loginServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });
  });

  it('should allow access if user is admin', () => {
    //if admin
    loginServiceSpy.isAdmin.and.returnValue(true);

    const result = executeGuard({} as any, {} as any);

    expect(result).toBe(true);
    expect(loginServiceSpy.isAdmin).toHaveBeenCalled();
  });

  it('should deny access and redirect if user is NOT admin', () => {
    // if no admin
    loginServiceSpy.isAdmin.and.returnValue(false);
    
    
    const dummyUrlTree = {} as UrlTree;
    routerSpy.createUrlTree.and.returnValue(dummyUrlTree);

    spyOn(window, 'alert'); 

    const result = executeGuard({} as any, {} as any);

    expect(result).toBe(dummyUrlTree); // Debe devolver la redirección
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/ACCESS DENIED/));
  });
});