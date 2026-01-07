import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should get users page', () => {
    service.getUsers(0, 10).subscribe();

    const req = httpMock.expectOne('/api/users?page=0&size=10');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });


  it('should change role to ADMIN', () => {
    service.changeRole(1, true).subscribe();

    const req = httpMock.expectOne('/api/users/1/role?role=ADMIN');
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should change role to USER', () => {
    service.changeRole(1, false).subscribe();

    const req = httpMock.expectOne('/api/users/1/role?role=USER');
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should toggle block status', () => {
    service.toggleBlock(1).subscribe();

    const req = httpMock.expectOne('/api/users/1/block');
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should delete user', () => {
    service.deleteUser(1).subscribe();

    const req = httpMock.expectOne('/api/users/1');
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});