import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageReservationsComponent } from '../manage-reservations/manage-reservations.component';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';

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

  const mockRooms = [{ id: 101, name: 'Lab 1', active: true, capacity: 20, camp: 'MOSTOLES', place: 'B1' }];

  beforeEach(async () => {
    reservationServiceSpy = jasmine.createSpyObj('ReservationService', ['getReservationsByUser', 'cancelReservation', 'updateReservation']);
    roomsServiceSpy = jasmine.createSpyObj('RoomsService', ['getRooms']);

    await TestBed.configureTestingModule({
      declarations: [ ManageReservationsComponent ],
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
  });

  it('should start editing a reservation', () => {
    const resToEdit = { id: 1, reason: 'Old', room: { id: 101, name: 'Lab 1' } };
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

  // --- PRUEBA CORREGIDA ---
  it('should save edit successfully', () => {
    spyOn(window, 'alert');
    // Preparamos los datos
    const dataToUpdate = { id: 1, reason: 'Updated' };
    component.editingReservation = dataToUpdate;
    
    reservationServiceSpy.updateReservation.and.returnValue(of({}));

    component.saveEdit();

    // CORRECCIÓN: Usamos 'dataToUpdate' en vez de 'component.editingReservation'
    // porque component.editingReservation se pone a null tras el éxito.
    expect(reservationServiceSpy.updateReservation).toHaveBeenCalledWith(1, dataToUpdate);
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/successfully/));
    expect(component.editingReservation).toBeNull(); 
  });

  it('should handle error when saving edit', () => {
    spyOn(window, 'alert');
    component.editingReservation = { id: 1 };
    reservationServiceSpy.updateReservation.and.returnValue(throwError(() => new Error('Error')));

    component.saveEdit();

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/error/));
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

  it('pagination: should calculate sliding window correctly', () => {
    component.pageData = { totalPages: 20 } as any;
    component.currentPage = 15; 
    const pages = component.getVisiblePages();
    expect(pages.length).toBe(10);
    expect(pages).toContain(15);
  });
});