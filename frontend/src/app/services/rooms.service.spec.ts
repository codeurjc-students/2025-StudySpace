import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RoomsService } from './rooms.service';

describe('RoomsService', () => {
  let service: RoomsService;
  let httpMock: HttpTestingController;
  //const BASE_URL = 'https://localhost:8443/api/rooms';
  const BASE_URL = '/api/rooms';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RoomsService]
    });
    service = TestBed.inject(RoomsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should get rooms with pagination', () => {
    service.getRooms(0, 10).subscribe();
    
    const req = httpMock.expectOne(`${BASE_URL}?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should get a single room with projection', () => {
    service.getRoom(1).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1?projection=withSoftware`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should create a room', () => {
    const newRoom = { name: 'Lab 1' };
    service.createRoom(newRoom).subscribe();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newRoom);
    req.flush({});
  });

  it('should update a room', () => {
    const updatedData = { name: 'Lab Updated' };
    service.updateRoom(1, updatedData).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedData);
    req.flush({});
  });

  it('should delete a room', () => {
    service.deleteRoom(1).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('should get room stats', () => {
    const date = '2023-10-01';
    service.getRoomStats(1, date).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1/stats?date=${date}`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});