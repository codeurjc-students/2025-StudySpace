import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { SoftwareService } from './software.service';

describe('SoftwareService', () => {
  let service: SoftwareService;
  let httpMock: HttpTestingController;
  const BASE_URL = '/api/softwares';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SoftwareService],
    });
    service = TestBed.inject(SoftwareService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); //varify no http requests
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

  it('should search softwares with parameters', () => {
    service.searchSoftwares('Java', 11.5, 0, 10).subscribe();
    const req = httpMock.expectOne(
      `/api/search/softwares?page=0&size=10&text=Java&minVersion=11.5`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should search softwares with partial parameters', () => {
    service.searchSoftwares(undefined, undefined, 2, 5).subscribe();
    const req = httpMock.expectOne(`/api/search/softwares?page=2&size=5`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
