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
    reservationServiceSpy = jasmine.createSpyObj('ReservationService', ['getReservationsByUser', 'deleteReservation', 'updateReservation']);
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
    
    //default answer
    reservationServiceSpy.getReservationsByUser.and.returnValue(of([
      { id: 1, reason: 'Test Booking', startDate: new Date(), endDate: new Date(), room: { id: 10, name: 'Lab 1' } }
    ]));
    roomsServiceSpy.getRooms.and.returnValue(of([{ id: 10, name: 'Lab 1' } as any]));

    fixture.detectChanges();//for ngOninit
  });

  it('should init correctly and load reservations', () => {
    expect(component).toBeTruthy();
    expect(component.userId).toBe(123);
    expect(reservationServiceSpy.getReservationsByUser).toHaveBeenCalledWith(123);
    expect(component.reservations.length).toBe(1);
    expect(roomsServiceSpy.getRooms).toHaveBeenCalled();
  });

  it('should delete reservation when confirmed', () => {//simulate Ok
    spyOn(window, 'confirm').and.returnValue(true);
    reservationServiceSpy.deleteReservation.and.returnValue(of({}));

    component.deleteReservation(1);

    expect(window.confirm).toHaveBeenCalled();
    expect(reservationServiceSpy.deleteReservation).toHaveBeenCalledWith(1);
    // Debe recargar la lista
    expect(reservationServiceSpy.getReservationsByUser).toHaveBeenCalledTimes(2); 
  });

  it('should NOT delete reservation when cancelled', () => {//simulate cancel
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
    component.editingReservation = { id: 1, reason: 'Updated Reason' };
    reservationServiceSpy.updateReservation.and.returnValue(of({}));

    component.saveEdit();

    expect(reservationServiceSpy.updateReservation).toHaveBeenCalledWith(1, component.editingReservation);
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/successfully/));
    expect(component.editingReservation).toBeNull(); //close edition
    expect(reservationServiceSpy.getReservationsByUser).toHaveBeenCalled(); 
  });

  it('should handle error when saving edits', () => {
    spyOn(window, 'alert');
    component.editingReservation = { id: 1 };
    reservationServiceSpy.updateReservation.and.returnValue(throwError(() => new Error('Error')));

    component.saveEdit();

    expect(window.alert).toHaveBeenCalledWith('Update error');
  });
});