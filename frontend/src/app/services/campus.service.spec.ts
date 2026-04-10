import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { CampusService } from './campus.service';
import { CampusDTO } from '../dtos/campus.dto';

describe('CampusService', () => {
  let service: CampusService;
  let httpMock: HttpTestingController;
  const BASE_URL = '/api/campus';

  const mockCampus: CampusDTO = {
    id: 1,
    name: 'Móstoles',
    coordinates: '40.33, -3.87',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CampusService],
    });

    service = TestBed.inject(CampusService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAllCampus: should make a GET request and return an array of campuses', () => {
    const mockCampuses: CampusDTO[] = [
      mockCampus,
      { id: 2, name: 'Alcorcón', coordinates: '40.35, -3.83' },
    ];

    service.getAllCampus().subscribe((campuses) => {
      expect(campuses.length).toBe(2);
      expect(campuses).toEqual(mockCampuses);
    });

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('GET');
    req.flush(mockCampuses);
  });

  it('createCampus: should make a POST request with the campus data', () => {
    service.createCampus(mockCampus).subscribe((campus) => {
      expect(campus).toEqual(mockCampus);
    });

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockCampus);
    req.flush(mockCampus);
  });

  it('updateCampus: should make a PUT request to the specific ID', () => {
    const updatedCampus: CampusDTO = {
      ...mockCampus,
      name: 'Móstoles Renovado',
    };

    service.updateCampus(1, updatedCampus).subscribe((campus) => {
      expect(campus.name).toBe('Móstoles Renovado');
    });

    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedCampus);
    req.flush(updatedCampus);
  });

  it('deleteCampus: should make a DELETE request to the specific ID', () => {
    service.deleteCampus(1).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
