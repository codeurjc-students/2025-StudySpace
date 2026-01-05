import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReservationService } from './reservation.service';

describe('ReservationService', () => {
  let service: ReservationService;
  let httpMock: HttpTestingController;

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
    const dummyPage = { content: [], totalPages: 1 };
    
    service.getReservationsByUser(1, 0, 5).subscribe(res => {
      expect(res).toEqual(dummyPage as any);
    });

    const req = httpMock.expectOne('/api/users/1/reservations?page=0&size=5');
    expect(req.request.method).toBe('GET');
    req.flush(dummyPage);
  });

  it('deleteReservation should call DELETE api', () => {
    service.deleteReservation(123).subscribe(res => {
      expect(res).toBeTruthy();
    });

    const req = httpMock.expectOne('/api/reservations/123');
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('updateReservation should call PUT api with data', () => {
    const updateData = { reason: 'New Reason' };
    
    service.updateReservation(10, updateData).subscribe(res => {
      expect(res).toEqual(updateData);
    });

    const req = httpMock.expectOne('/api/reservations/10');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateData);
    req.flush(updateData);
  });

  it('createReservation should post correct body', () => {
    const roomId = 5;
    const startDate = new Date();
    const endDate = new Date();
    const reason = 'Meeting';

    service.createReservation(roomId, startDate, endDate, reason).subscribe();

    const req = httpMock.expectOne('/api/reservations/create');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      roomId, startDate, endDate, reason
    });
    req.flush({});
  });

  it('getMyReservations should call correct URL', () => {
    service.getMyReservations(1, 20).subscribe();

    const req = httpMock.expectOne('/api/reservations/my-reservations?page=1&size=20');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('cancelReservation should call PATCH api', () => {
    service.cancelReservation(55).subscribe();

    const req = httpMock.expectOne('/api/reservations/55/cancel');
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });
});