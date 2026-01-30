import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RoomsService } from './rooms.service';

describe('RoomsService', () => {
  let service: RoomsService;
  let httpMock: HttpTestingController;
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

  it('should delete a room', () => {
    service.deleteRoom(1, "").subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1?reason=`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('should delete a room with a reason', () => {
    const reason = 'Mantenimiento preventivo';
    service.deleteRoom(1, reason).subscribe();
    
    const encodedReason = encodeURIComponent(reason);
    const req = httpMock.expectOne(`${BASE_URL}/1?reason=${encodedReason}`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('should upload a room image using FormData', () => {
    const roomId = 1;
    const mockFile = new File([''], 'room.jpg', { type: 'image/jpeg' });
    service.uploadRoomImage(roomId, mockFile).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/${roomId}/image`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({});
  });

  it('should create a room', () => {
    const newRoom = { name: 'Lab 1' };
    service.createRoom(newRoom).subscribe();
    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should get a single room with projection', () => {
    const roomId = 123;
    service.getRoom(roomId).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/${roomId}?projection=withSoftware`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: roomId, name: 'Test Room' });
  });

  it('should update room data', () => {
    const roomId = 1;
    const updateData = { name: 'Updated Name', capacity: 50 };
    
    service.updateRoom(roomId, updateData).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/${roomId}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateData);
    req.flush({});
  });

  it('should get room stats for a specific date', () => {
    const roomId = 1;
    const date = '2024-05-20';
    
    service.getRoomStats(roomId, date).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/${roomId}/stats?date=${date}`);
    expect(req.request.method).toBe('GET');
    req.flush({ totalReservations: 5, usageHours: 10 });
  });

  it('should handle getRoom with string ID', () => {
    const stringId = 'abc-123';
    service.getRoom(stringId).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/${stringId}?projection=withSoftware`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});