import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ReservationService } from './reservation.service';

describe('ReservationService', () => {
  let service: ReservationService;
  let httpMock: HttpTestingController;
  const BASE_URL = '/api/reservations';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ReservationService],
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

  it('should get reservations by user', () => {
    service.getReservationsByUser(1, 0, 10).subscribe();
    const req = httpMock.expectOne(`/api/users/1/reservations?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should delete reservation', () => {
    service.deleteReservation(123).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/123`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('should update a reservation', () => {
    const mockRes = { reason: 'update' };
    service.updateReservation(1, mockRes).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(mockRes);
    req.flush({});
  });

  it('should create a reservation', () => {
    const startDate = new Date();
    const endDate = new Date();
    service.createReservation(1, startDate, endDate, 'test reason').subscribe();
    const req = httpMock.expectOne(`${BASE_URL}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      roomId: 1,
      startDate: startDate,
      endDate: endDate,
      reason: 'test reason',
    });
    req.flush({});
  });

  it('should get my reservations', () => {
    service.getMyReservations(0, 10).subscribe();
    const req = httpMock.expectOne(
      `${BASE_URL}/my-reservations?page=0&size=10`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should cancel a reservation', () => {
    service.cancelReservation(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/cancel`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({});
    req.flush({});
  });

  it('should check availability', () => {
    service.checkAvailability(5, '2026-03-01').subscribe();
    const req = httpMock.expectOne(
      `${BASE_URL}/check-availability?roomId=5&date=2026-03-01`,
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should update reservation admin', () => {
    const updateData = { roomId: 5, adminReason: 'Moved' };
    service.updateReservationAdmin(1, updateData).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/admin/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateData);
    req.flush({});
  });

  it('should cancel reservation admin', () => {
    const reason = 'Maintenance issue';
    service.cancelReservationAdmin(123, reason).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/admin/123/cancel`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ reason });
    req.flush({});
  });

  it('should cancel reservation as admin', () => {
    service.cancelReservationAdmin(1, 'Maintenance').subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/admin/1/cancel`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ reason: 'Maintenance' });
    req.flush({});
  });

  it('should search reservations admin with filters', () => {
    service.searchReservationsAdmin(1, 'Exam', '2026-05-05', 0, 10).subscribe();
    const req = httpMock.expectOne(
      `/api/search/reservations/user/1?page=0&size=10&text=Exam&date=2026-05-05`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should search my reservations with filters', () => {
    service.searchMyReservations('Lab', '2026-10-10', 1, 5).subscribe();
    const req = httpMock.expectOne(
      `/api/search/reservations/me?page=1&size=5&text=Lab&date=2026-10-10`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should perform smart search', () => {
    const start = new Date('2026-05-05T10:00:00Z');
    const end = new Date('2026-05-05T12:00:00Z');
    service.smartSearch(start, end, 20, 1).subscribe();

    const req = httpMock.expectOne(
      `${BASE_URL}/smart-search?start=${start.toISOString()}&end=${end.toISOString()}&minCapacity=20&campusId=1`,
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
