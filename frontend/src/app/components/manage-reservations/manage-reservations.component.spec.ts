import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageReservationsComponent } from './manage-reservations.component';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';

describe('ManageReservationsComponent', () => {
  let component: ManageReservationsComponent;
  let fixture: ComponentFixture<ManageReservationsComponent>;
  
  //to simulate services (spys)
  let reservationServiceSpy: jasmine.SpyObj<ReservationService>;
  let roomsServiceSpy: jasmine.SpyObj<RoomsService>;

  beforeEach(async () => {
    //mocks
    reservationServiceSpy = jasmine.createSpyObj('ReservationService', ['getReservationsByUser', 'deleteReservation', 'updateReservation','cancelReservation']);
    roomsServiceSpy = jasmine.createSpyObj('RoomsService', ['getRooms']);

    await TestBed.configureTestingModule({
      declarations: [ ManageReservationsComponent ], 
      imports: [ FormsModule ], 
      providers: [
        { provide: ReservationService, useValue: reservationServiceSpy },
        { provide: RoomsService, useValue: roomsServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => '123' //id 123
              }
            }
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageReservationsComponent);
    component = fixture.componentInstance;
    
    //default answer - return Page objects
    reservationServiceSpy.getReservationsByUser.and.returnValue(of({
      content: [ { id: 1, reason: 'Test Booking', startDate: new Date(), endDate: new Date(), room: { id: 10, name: 'Lab 1' } } ],
      totalPages: 1,
      totalElements: 1,
      last: true,
      first: true,
      size: 10,
      number: 0,
      numberOfElements: 1,
      sort: []
    }));
    roomsServiceSpy.getRooms.and.returnValue(of({
      content: [ { id: 10, name: 'Lab 1' } ],
      totalPages: 1,
      totalElements: 1,
      last: true,
      first: true,
      size: 10,
      number: 0,
      numberOfElements: 1,
      sort: []
    } as any));

    fixture.detectChanges();//for ngOninit
  });

  it('should init correctly and load reservations', () => {
    expect(component).toBeTruthy();
    expect(component.userId).toBe(123);
    expect(reservationServiceSpy.getReservationsByUser).toHaveBeenCalledWith(123, 0);
    expect(component.reservations.length).toBe(1);
    expect(roomsServiceSpy.getRooms).toHaveBeenCalled();
  });

  it('should delete reservation when confirmed', () => {//simulate Ok
    spyOn(window, 'confirm').and.returnValue(true);
    reservationServiceSpy.deleteReservation.and.returnValue(of({}));

    component.deleteReservation(1);

    expect(window.confirm).toHaveBeenCalled();
    expect(reservationServiceSpy.deleteReservation).toHaveBeenCalledWith(1);
    expect(reservationServiceSpy.getReservationsByUser).toHaveBeenCalledTimes(2); 
  });

  it('should NOT delete reservation when cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.deleteReservation(1);

    expect(reservationServiceSpy.deleteReservation).not.toHaveBeenCalled();
  });

  it('should prepare reservation for editing', () => {
    const mockRes = { id: 5, room: { id: 99, name: 'Sala X' } };
    component.startEdit(mockRes);

    //Verify
    expect(component.editingReservation).toBeTruthy();
    expect(component.editingReservation.id).toBe(5);
    expect(component.editingReservation.roomId).toBe(99);
  });

  it('should save edits successfully', () => {
    spyOn(window, 'alert'); 
    const reservationData = { id: 1, reason: 'Updated Reason' };//to avoid null
    component.editingReservation = reservationData;
    reservationServiceSpy.updateReservation.and.returnValue(of({}));

    component.saveEdit();

    expect(reservationServiceSpy.updateReservation).toHaveBeenCalledWith(1, reservationData);
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/successfully/));
    expect(component.editingReservation).toBeNull(); //close edition
    expect(reservationServiceSpy.getReservationsByUser).toHaveBeenCalled(); //reload
  });

  it('should handle error when saving edits', () => {
    spyOn(window, 'alert');
    component.editingReservation = { id: 1 };
    reservationServiceSpy.updateReservation.and.returnValue(throwError(() => new Error('Error')));

    component.saveEdit();

    expect(window.alert).toHaveBeenCalledWith('Update error');
  });
  it('should clear editingReservation on cancelEdit', () => {
    component.editingReservation = { id: 1 };
    component.cancelEdit();
    expect(component.editingReservation).toBeNull();
  });













  it('should prepare reservation for editing and handle room object', () => {
    const mockRes = { id: 5, room: { id: 99, name: 'Sala X' } };
    component.startEdit(mockRes);
    expect(component.editingReservation.roomId).toBe(99);
  });
  it('should clear editingReservation on cancelEdit', () => {
    component.editingReservation = { id: 1 };
    component.cancelEdit();
    expect(component.editingReservation).toBeNull();
  });

  it('should prepare reservation for editing and handle room object', () => {
    const mockRes = { id: 5, room: { id: 99, name: 'Sala X' } };
    component.startEdit(mockRes);
    expect(component.editingReservation.roomId).toBe(99);
  });

  it('isReservationActive should return true for future non-cancelled reservations', () => {
    const futureDate = new Date();
    futureDate.setFullYear(futureDate.getFullYear() + 1);
    const res = { endDate: futureDate, cancelled: false };
    expect(component.isReservationActive(res)).toBeTrue();
  });
  it('isReservationActive should return false for past or cancelled reservations', () => {
    const pastDate = new Date();
    pastDate.setFullYear(pastDate.getFullYear() - 1);
    expect(component.isReservationActive({ endDate: pastDate, cancelled: false })).toBeFalse();
    expect(component.isReservationActive({ endDate: new Date(), cancelled: true })).toBeFalse();
    expect(component.isReservationActive(null)).toBeFalse();
  });


  it('should performCancel when confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    reservationServiceSpy.cancelReservation.and.returnValue(of({}));
    
    component.performCancel(1);
    
    expect(reservationServiceSpy.cancelReservation).toHaveBeenCalledWith(1);
    expect(reservationServiceSpy.getReservationsByUser).toHaveBeenCalledTimes(2);
  });
  it('should handle error in performCancel', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    reservationServiceSpy.cancelReservation.and.returnValue(throwError(() => new Error('Err')));
    
    component.performCancel(1);
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/error/));
  });

  it('should delete reservation when confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    reservationServiceSpy.deleteReservation.and.returnValue(of({}));
    component.deleteReservation(1);
    expect(reservationServiceSpy.deleteReservation).toHaveBeenCalledWith(1);
  });

});