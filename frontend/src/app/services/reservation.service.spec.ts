import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReservationService } from './reservation.service';

describe('ReservationService', () => {
  let service: ReservationService;
  let httpMock: HttpTestingController;
  
  const BASE_URL = 'https://localhost:8443/api/reservations';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ReservationService]
    });
    service = TestBed.inject(ReservationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); 
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getReservationsByUser should call correct URL', () => {
    service.getReservationsByUser(1, 0, 5).subscribe();
    
    const req = httpMock.expectOne(`/api/users/1/reservations?page=0&size=5`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('deleteReservation should call DELETE api', () => {
    service.deleteReservation(123).subscribe();

    const req = httpMock.expectOne(req => req.url.includes('/api/reservations/123'));
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('updateReservation should call PUT api with data', () => {
    const updateData = { reason: 'Changed' };
    service.updateReservation(10, updateData).subscribe();

    const req = httpMock.expectOne(req => req.url.includes('/api/reservations/10'));
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateData);
    req.flush({});
  });

  it('createReservation should post correct body structure', () => {
    const roomId = 5;
    const startDate = new Date('2026-01-01T10:00:00');
    const endDate = new Date('2026-01-01T12:00:00');
    const reason = 'Meeting';

    service.createReservation(roomId, startDate, endDate, reason).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      roomId, startDate, endDate, reason
    });
    req.flush({});
  });

  it('getMyReservations should call correct URL', () => {
    service.getMyReservations(0, 10).subscribe();

    const req = httpMock.expectOne(req => req.url.includes('/my-reservations'));
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('cancelReservation should call PATCH api', () => {
    service.cancelReservation(55).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/55/cancel`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('checkAvailability should call GET with query params', () => {
    const roomId = 1;
    const dateStr = '2026-01-31';

    service.checkAvailability(roomId, dateStr).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/check-availability?roomId=1&date=2026-01-31`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});