import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SoftwareService } from './software.service';

describe('SoftwareService', () => {
  let service: SoftwareService;
  let httpMock: HttpTestingController;
  const BASE_URL = '/api/softwares';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SoftwareService]
    });
    service = TestBed.inject(SoftwareService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should retrieve all softwares', () => {
    service.getAllSoftwares(0, 10).subscribe();
    
    const req = httpMock.expectOne(`${BASE_URL}?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should retrieve one software by ID', () => {
    service.getSoftware(1).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should create software', () => {
    const data = { name: 'Java' };
    service.createSoftware(data).subscribe();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(data);
    req.flush({});
  });

  it('should update software', () => {
    const data = { name: 'Java 17' };
    service.updateSoftware(1, data).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(data);
    req.flush({});
  });

  it('should delete software', () => {
    service.deleteSoftware(1).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});