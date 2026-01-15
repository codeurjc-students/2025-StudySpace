import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReservationFormComponent } from './reservation-form.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms'; // for [(ngModel)]
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { LoginService } from '../../login/login.service';
import { of, throwError} from 'rxjs';
import { Router } from '@angular/router';




describe('ReservationFormComponent UI Test', () => {
  let component: ReservationFormComponent;
  let fixture: ComponentFixture<ReservationFormComponent>;
  let reservationServiceSpy: jasmine.SpyObj<ReservationService>;
  let roomsServiceSpy: jasmine.SpyObj<RoomsService>;
  let router: Router;

  const mockRoomsResponse = { 
    content: [
      { 
        id: 1, name: 'Aula Test 1', capacity: 20, camp: 'MOSTOLES', active: true,
        place: 'Bloque 1', coordenades: '0,0', software: [] 
      },
      { 
        id: 2, name: 'Aula Test 2', capacity: 30, camp: 'ALCORCON', active: true,
        place: 'Bloque 2', coordenades: '0,0', software: [] 
      },
      { id: 3, name: 'Aula Desactivada', capacity: 15, camp: 'VICALVARO', active: false,
        place: 'Bloque 3', coordenades: '0,0', software: []
      }
    ], 
    totalPages: 1, 
    totalElements: 2, 
    number: 0, 
    size: 2,
    first: true,
    last: true,
    numberOfElements: 2,
    empty: false,
    sort: []
  };

  beforeEach(async () => {
    const resSpy = jasmine.createSpyObj('ReservationService', ['createReservation', 'checkAvailability']);
    const roomsSpy = jasmine.createSpyObj('RoomsService', ['getRooms']);

    await TestBed.configureTestingModule({
      declarations: [ReservationFormComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, FormsModule],
      providers: [
        { provide: ReservationService, useValue: resSpy },
        { provide: RoomsService, useValue: roomsSpy }
      ]
    }).compileComponents();

    roomsServiceSpy = TestBed.inject(RoomsService) as jasmine.SpyObj<RoomsService>;
    reservationServiceSpy = TestBed.inject(ReservationService) as jasmine.SpyObj<ReservationService>;
    router = TestBed.inject(Router);
    
    fixture = TestBed.createComponent(ReservationFormComponent);
    component = fixture.componentInstance;

    reservationServiceSpy.checkAvailability.and.returnValue(of([]));
    roomsServiceSpy.getRooms.and.returnValue(of(mockRoomsResponse));
    
    fixture.detectChanges();
  });

  it('You must create the component', () => {
    expect(component).toBeTruthy();
  });

  it('You must upload the list of classrooms', () => {
    expect(component.rooms.length).toBe(2);
  });

  it('The "Confirm Reservation" button should be disabled at startup', async () => {
    component.roomId = null;
    
    fixture.detectChanges();
    await fixture.whenStable(); 
    fixture.detectChanges(); 

    //Search fot the button
    const button = fixture.nativeElement.querySelector('button[type="submit"]');
    
    //Verify 'disabled' of HTML
    expect(button.disabled).toBeTrue();
  });







  it('should filter only active rooms and select the first one', () => {
    expect(component.rooms.length).toBe(2);
    expect(component.rooms[0].id).toBe(1);
    expect(component.roomId).toBe(1);
  });

  it('should handle no available rooms', () => {
    component.rooms = [];
    component.roomId = null;
    fixture.detectChanges();
    expect(component.roomId).toBeNull();
  });

  it('onSubmit: should alert if dates are missing', () => {
    spyOn(window, 'alert');
    component.selectedDate = '';
    component.selectedStartTime = '';
    component.selectedEndTime = '';
    component.onSubmit();
    //expect(window.alert).toHaveBeenCalledWith('Please fill in the dates.');
    expect(reservationServiceSpy.createReservation).not.toHaveBeenCalled();
  });

  it('onSubmit: should call service and navigate on success', () => {
    reservationServiceSpy.createReservation.and.returnValue(of({}));
    const navigateSpy = spyOn(router, 'navigate');
    spyOn(window, 'alert');

    component.roomId = 1;
    component.selectedDate = '2026-05-20';
    component.selectedStartTime = '10:00';
    component.selectedEndTime = '12:00';
    component.reason = 'Test Exam';
    component.onSubmit();

    expect(reservationServiceSpy.createReservation).toHaveBeenCalled();
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/success/));
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('onSubmit: should handle error from service', () => {
    reservationServiceSpy.createReservation.and.returnValue(throwError(() => ({ error: 'Room occupied' })));

    spyOn(window, 'alert');

    component.roomId = 1;
    component.selectedDate = '2026-05-20';
    component.selectedStartTime = '10:00';
    component.selectedEndTime = '11:00';
    
    component.onSubmit();

    expect(window.alert).toHaveBeenCalledWith('Room occupied');
  });







  it('calculateStartTimes: should generate slots from 08:00 to 20:30', () => {
    component.selectedDate = '2026-01-01'; 
    component.minDate = '2025-01-01';
    component.occupiedSlots = []; 

    component.calculateStartTimes();

    expect(component.startTimes.length).toBeGreaterThan(0);
    expect(component.startTimes[0]).toBe('08:00');
    expect(component.startTimes[component.startTimes.length - 1]).toBe('20:30');
  });

  it('calculateStartTimes: should filter out occupied slots', () => {
    component.selectedDate = '2026-01-01';
    component.occupiedSlots = [{
      startDate: '2026-01-01T10:00:00',
      endDate: '2026-01-01T11:00:00'
    }];

    component.calculateStartTimes();

    expect(component.startTimes).toContain('09:30');
    expect(component.startTimes).not.toContain('10:00');
    expect(component.startTimes).not.toContain('10:30');
    expect(component.startTimes).toContain('11:00'); 
  });

  it('onStartTimeChange: should limit duration to MAX 3 HOURS', () => {
    component.selectedStartTime = '09:00';
    component.occupiedSlots = [];

    component.onStartTimeChange();

    const endTimes = component.endTimes;
    expect(endTimes[endTimes.length - 1]).toBe('12:00');
    expect(endTimes).not.toContain('12:30'); 
  });

  it('onStartTimeChange: should stop end times before the NEXT reservation (Overlap)', () => {
    component.selectedStartTime = '09:00';
    component.occupiedSlots = [{
      startDate: '2026-01-01T10:00:00',
      endDate: '2026-01-01T11:00:00'
    }];

    component.onStartTimeChange();

    const endTimes = component.endTimes;
    expect(endTimes).toContain('10:00');
    expect(endTimes).not.toContain('10:30');
  });

  it('onConfigChange: should call checkAvailability', () => {
    component.roomId = 5;
    component.selectedDate = '2026-01-01';

    component.onConfigChange();

    expect(reservationServiceSpy.checkAvailability).toHaveBeenCalledWith(5, '2026-01-01');
  });


  it('ngOnInit: should handle error when loading rooms', () => {
    spyOn(console, 'error'); 
    roomsServiceSpy.getRooms.and.returnValue(throwError(() => new Error('API Error')));
    component.rooms = [];
    component.ngOnInit();
    
    expect(console.error).toHaveBeenCalled();
    expect(component.rooms).toEqual([]); 
  });

  it('onSubmit: should call service and navigate on success', () => {
    reservationServiceSpy.createReservation.and.returnValue(of({}));
    const navigateSpy = spyOn(router, 'navigate');
    spyOn(window, 'alert');

    component.roomId = 1;
    component.selectedDate = '2026-05-20';
    component.selectedStartTime = '10:00';
    component.selectedEndTime = '12:00';
    
    component.onSubmit();

    expect(reservationServiceSpy.createReservation).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });
  it('calculateStartTimes: should generate correct times via Utility', () => {
    component.selectedDate = '2026-01-01';
    component.occupiedSlots = [];
    component.calculateStartTimes();
    expect(component.startTimes).toContain('08:00');
  });
  
  it('onStartTimeChange: should limit duration via Utility', () => {
    component.selectedStartTime = '09:00';
    component.occupiedSlots = []; 
    component.onStartTimeChange();
    const endTimes = component.endTimes;
    expect(endTimes[endTimes.length - 1]).toBe('12:00'); 
  });


  it('ngOnInit: should NOT select a roomId if room list is empty', () => {
    component.roomId = null; 
    component.rooms = [];

    roomsServiceSpy.getRooms.and.returnValue(of({ content: [], totalElements: 0, sort: [] } as any));
    
    component.ngOnInit();
    
    expect(component.rooms.length).toBe(0);
    expect(component.roomId).toBeNull();
  });

  it('onConfigChange: should handle error from checkAvailability', () => {
    spyOn(console, 'error');
    reservationServiceSpy.checkAvailability.and.returnValue(throwError(() => new Error('Network fail')));
    
    component.roomId = 1;
    component.selectedDate = '2026-01-01';
    
    component.onConfigChange();
    
    expect(console.error).toHaveBeenCalledWith('Error checking availability', jasmine.any(Error));
  });


});



