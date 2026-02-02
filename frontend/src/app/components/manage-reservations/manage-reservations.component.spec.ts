import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageReservationsComponent } from './manage-reservations.component';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { PaginationComponent } from '../pagination/pagination.component';

describe('ManageReservationsComponent', () => {
  let component: ManageReservationsComponent;
  let fixture: ComponentFixture<ManageReservationsComponent>;
  let reservationServiceMock: jasmine.SpyObj<ReservationService>;
  let roomsServiceMock: jasmine.SpyObj<RoomsService>;

  beforeEach(async () => {
    reservationServiceMock = jasmine.createSpyObj('ReservationService', [
      'getReservationsByUser', 
      'checkAvailability', 
      'deleteReservation',
      'cancelReservationAdmin', 
      'updateReservationAdmin'
    ]);
    roomsServiceMock = jasmine.createSpyObj('RoomsService', ['getRooms']);

    await TestBed.configureTestingModule({
      declarations: [ ManageReservationsComponent, PaginationComponent ],
      imports: [ FormsModule, RouterTestingModule ],
      providers: [
        { provide: ReservationService, useValue: reservationServiceMock },
        { provide: RoomsService, useValue: roomsServiceMock },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '123' } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ManageReservationsComponent);
    component = fixture.componentInstance;
    
    // Espías de ventana centralizados
    spyOn(window, 'prompt');
    spyOn(window, 'confirm');
    spyOn(window, 'alert');
    spyOn(console, 'error');

    // Mocks de datos iniciales
    const mockRes = { 
      id: 1, 
      roomId: 10, 
      startDate: '2026-01-01T10:00:00', 
      endDate: '2026-01-01T12:00:00',
      room: { name: 'Lab Test' },
      cancelled: false
    };

    reservationServiceMock.getReservationsByUser.and.returnValue(of({ 
        content: [mockRes], 
        totalPages: 1, 
        number: 0, 
        size: 10, 
        totalElements: 1 
    } as any));
    
    roomsServiceMock.getRooms.and.returnValue(of({ content: [{id: 10, name: 'Lab 1'}] } as any));
    
    fixture.detectChanges();
  });

  it('should create and load initial data (user and rooms)', () => {
    expect(component).toBeTruthy();
    expect(component.userId).toBe(123);
    expect(component.reservations.length).toBe(1);
    expect(component.rooms.length).toBe(1);
  });

  it('startEdit: should parse date and time correctly from ISO string', () => {
    const mockRes = {
      id: 1,
      roomId: 101,
      startDate: '2026-01-31T14:30:00',
      endDate: '2026-01-31T16:00:00'
    };
    reservationServiceMock.checkAvailability.and.returnValue(of([]));

    component.startEdit(mockRes);

    expect(component.editDateStr).toBe('2026-01-31');
    expect(component.editStartTime).toBe('14:30');
    expect(component.editEndTime).toBe('16:00');
  });

  it('onConfigChange: should exclude the current reservation from occupied slots', () => {
    component.editingReservation = { id: 99, roomId: 1 };
    component.editDateStr = '2026-01-01';
    const backendResponse = [
      { id: 99, startDate: '2026-01-01T10:00:00' }, 
      { id: 100, startDate: '2026-01-01T12:00:00' } 
    ];
    reservationServiceMock.checkAvailability.and.returnValue(of(backendResponse));

    component.onConfigChange();

    expect(component.occupiedSlots.length).toBe(1);
    expect(component.occupiedSlots[0].id).toBe(100);
  });

  it('saveEdit: should call updateReservationAdmin with correct data and notify success', () => {
    component.editingReservation = { id: 50, roomId: 101 };
    component.editDateStr = '2026-05-20';
    component.editStartTime = '09:00';
    component.editEndTime = '11:00';
    component.adminModificationReason = 'Test Reason';
    
    reservationServiceMock.updateReservationAdmin.and.returnValue(of({}));
    component.saveEdit();

    expect(reservationServiceMock.updateReservationAdmin).toHaveBeenCalledWith(50, jasmine.objectContaining({
      adminReason: 'Test Reason'
    }));
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/updated/i));
  });

  it('performCancel: should call cancelReservationAdmin with reason when confirmed', () => {
    const reason = "Violation of rules";
    (window.prompt as jasmine.Spy).and.returnValue(reason);
    (window.confirm as jasmine.Spy).and.returnValue(true);
    reservationServiceMock.cancelReservationAdmin.and.returnValue(of({}));

    component.performCancel(1);

    expect(reservationServiceMock.cancelReservationAdmin).toHaveBeenCalledWith(1, reason);
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/cancelled/i));
  });

  it('deleteReservation: should call deleteReservation service if confirmed', () => {
    (window.confirm as jasmine.Spy).and.returnValue(true);
    reservationServiceMock.deleteReservation.and.returnValue(of({}));

    component.deleteReservation(1);
    expect(reservationServiceMock.deleteReservation).toHaveBeenCalledWith(1);
  });

  it('isReservationActive: should correctly identify future non-cancelled reservations', () => {
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 1);
    
    const activeRes = { endDate: futureDate.toISOString(), cancelled: false };
    const cancelledRes = { endDate: futureDate.toISOString(), cancelled: true };
    const pastRes = { endDate: '2000-01-01T10:00:00', cancelled: false };

    expect(component.isReservationActive(activeRes)).toBeTrue();
    expect(component.isReservationActive(cancelledRes)).toBeFalse();
    expect(component.isReservationActive(pastRes)).toBeFalse();
  });

  it('pagination logic: should calculate visible pages', () => {
    component.pageData = { totalPages: 20 } as any;
    component.currentPage = 5; 
    const pages = component.getVisiblePages();
    expect(pages).toContain(5);
  });

  it('handle errors: should log errors on load fail', () => {
    reservationServiceMock.getReservationsByUser.and.returnValue(throwError(() => new Error('Load failed')));
    component.loadReservations(0);
    expect(console.error).toHaveBeenCalled();
  });
});