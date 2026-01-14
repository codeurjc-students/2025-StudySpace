import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageReservationsComponent } from './manage-reservations.component';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { RoomDTO } from '../../dtos/room.dto';
import { PaginationComponent } from '../pagination/pagination.component';

describe('ManageReservationsComponent', () => {
  let component: ManageReservationsComponent;
  let fixture: ComponentFixture<ManageReservationsComponent>;
  
  let reservationServiceSpy: jasmine.SpyObj<ReservationService>;
  let roomsServiceSpy: jasmine.SpyObj<RoomsService>;

  const mockPageData = {
    content: [
      { id: 1, reason: 'Test 1', startDate: new Date().toISOString(), endDate: new Date().toISOString(), cancelled: false },
      { id: 2, reason: 'Test 2', startDate: new Date().toISOString(), endDate: new Date().toISOString(), cancelled: true }
    ],
    totalPages: 5,
    number: 0,
    size: 10,
    first: true,
    last: false,
    totalElements: 50
  };

  
  const mockRooms: RoomDTO[] = [{ 
      id: 101, 
      name: 'Lab 1', 
      active: true, 
      capacity: 20, 
      camp: 'MOSTOLES', 
      place: 'B1',
      coordenades: '0,0', 
      software: []        
  }];

  beforeEach(async () => {
    reservationServiceSpy = jasmine.createSpyObj('ReservationService', ['getReservationsByUser', 
      'cancelReservation', 
      'updateReservation',
      'deleteReservation',
      'checkAvailability']);
    roomsServiceSpy = jasmine.createSpyObj('RoomsService', ['getRooms']);

    await TestBed.configureTestingModule({
      declarations: [ ManageReservationsComponent, PaginationComponent ],
      imports: [ FormsModule, RouterTestingModule ],
      providers: [
        { provide: ReservationService, useValue: reservationServiceSpy },
        { provide: RoomsService, useValue: roomsServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '123' } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ManageReservationsComponent);
    component = fixture.componentInstance;
    
    reservationServiceSpy.getReservationsByUser.and.returnValue(of(mockPageData as any));
    roomsServiceSpy.getRooms.and.returnValue(of({ content: mockRooms } as any));
    
    fixture.detectChanges();
  });

  it('should create and load initial data', () => {
    expect(component).toBeTruthy();
    expect(component.userId).toBe(123);
    expect(component.reservations.length).toBe(2);
    expect(component.rooms.length).toBe(1);
  });

  it('should start editing a reservation', () => {
    const resToEdit = { 
      id: 1, 
      reason: 'Old', 
      room: { id: 101, name: 'Lab 1' },
      startDate: '2026-01-01T10:00:00',
      endDate: '2026-01-01T12:00:00'
    };
    reservationServiceSpy.checkAvailability.and.returnValue(of([]));

    component.startEdit(resToEdit);

    expect(component.editingReservation).toBeDefined();
    expect(component.editingReservation.id).toBe(1);
    expect(component.editingReservation.roomId).toBe(101);
  });

  it('should cancel edit', () => {
    component.editingReservation = { id: 1 };
    component.cancelEdit();
    expect(component.editingReservation).toBeNull();
  });

  it('should save edit successfully', () => {
    spyOn(window, 'alert');
    const dataToUpdate = { id: 1, reason: 'Updated' };
    component.editingReservation = dataToUpdate;
    reservationServiceSpy.updateReservation.and.returnValue(of({}));

    component.saveEdit();

    expect(reservationServiceSpy.updateReservation).toHaveBeenCalledWith(1, dataToUpdate);
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/successfully/));
    expect(component.editingReservation).toBeNull();
  });

  it('should handle error when saving edit', () => {
    spyOn(window, 'alert');
    component.editingReservation = { id: 1 };
    reservationServiceSpy.updateReservation.and.returnValue(throwError(() => new Error('Error')));

    component.saveEdit();

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/error/i));
  });

  it('should perform cancel if confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    reservationServiceSpy.cancelReservation.and.returnValue(of({}));

    component.performCancel(1);

    expect(reservationServiceSpy.cancelReservation).toHaveBeenCalledWith(1);
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/successfully/));
  });

  it('should NOT perform cancel if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.performCancel(1);
    expect(reservationServiceSpy.cancelReservation).not.toHaveBeenCalled();
  });

  it('should correctly identify active reservations', () => {
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 1);
    
    const activeRes = { endDate: futureDate.toISOString(), cancelled: false };
    const cancelledRes = { endDate: futureDate.toISOString(), cancelled: true };
    const pastRes = { endDate: '2000-01-01', cancelled: false };

    expect(component.isReservationActive(activeRes)).toBeTrue();
    expect(component.isReservationActive(cancelledRes)).toBeFalse();
    expect(component.isReservationActive(pastRes)).toBeFalse();
  });

  it('pagination logic', () => {
    component.pageData = { totalPages: 20 } as any;
    component.currentPage = 15; 
    const pages = component.getVisiblePages();
    expect(pages.length).toBe(10);
    expect(pages).toContain(15);
  });




  it('startEdit: should parse date and time correctly from ISO string', () => {
    const mockRes = {
      id: 1,
      roomId: 101,
      room: { id: 101 },
      startDate: '2026-01-31T14:30:00',
      endDate: '2026-01-31T16:00:00'
    };
    
    reservationServiceSpy.checkAvailability.and.returnValue(of([]));

    component.startEdit(mockRes);

    expect(component.editDateStr).toBe('2026-01-31');
    expect(component.editStartTime).toBe('14:30');
    expect(component.editEndTime).toBe('16:00');
  });

  it('onConfigChange: should EXCLUDE the current reservation from occupied slots', () => {
    component.editingReservation = { id: 99, roomId: 1 };
    component.editDateStr = '2026-01-01';

    const backendResponse = [
      { id: 99, startDate: '2026-01-01T10:00:00', endDate: '2026-01-01T11:00:00' }, 
      { id: 100, startDate: '2026-01-01T12:00:00', endDate: '2026-01-01T13:00:00' } 
    ];

    reservationServiceSpy.checkAvailability.and.returnValue(of(backendResponse));

    component.onConfigChange();

    expect(component.occupiedSlots.length).toBe(1);
    expect(component.occupiedSlots[0].id).toBe(100);
  });

  it('should maintain selected end time if valid after config change', () => {
    component.editStartTime = '09:00';
    component.editEndTime = '10:00';
    
    reservationServiceSpy.checkAvailability.and.returnValue(of([]));
    
    component.editingReservation = { id: 1, roomId: 1 };
    component.editDateStr = '2026-01-01';
    component.onConfigChange(true);

    expect(component.editEndTime).toBe('10:00');
  });


});