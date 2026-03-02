import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { ReservationFormComponent } from './reservation-form.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms'; // for [(ngModel)]
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { LoginService } from '../../login/login.service';
import { of, throwError } from 'rxjs';
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
        id: 1,
        name: 'Aula Test 1',
        capacity: 20,
        camp: 'MOSTOLES',
        active: true,
        place: 'Bloque 1',
        coordenades: '0,0',
        software: [],
      },
      {
        id: 2,
        name: 'Aula Test 2',
        capacity: 30,
        camp: 'ALCORCON',
        active: true,
        place: 'Bloque 2',
        coordenades: '0,0',
        software: [],
      },
      {
        id: 3,
        name: 'Aula Desactivada',
        capacity: 15,
        camp: 'VICALVARO',
        active: false,
        place: 'Bloque 3',
        coordenades: '0,0',
        software: [],
      },
    ],
    totalPages: 1,
    totalElements: 2,
    number: 0,
    size: 2,
    first: true,
    last: true,
    numberOfElements: 2,
    empty: false,
    sort: [],
  };

  beforeEach(async () => {
    reservationServiceSpy = jasmine.createSpyObj('ReservationService', [
      'checkAvailability',
      'createReservation',
      'smartSearch',
    ]);
    roomsServiceSpy = jasmine.createSpyObj('RoomsService', [
      'getRooms',
      'checkAvailability',
      'searchRooms',
    ]);

    roomsServiceSpy.searchRooms.and.returnValue(
      of({
        content: mockRoomsResponse.content,
        number: 0,
        totalPages: 1,
      } as any),
    );

    await TestBed.configureTestingModule({
      declarations: [ReservationFormComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, FormsModule],
      providers: [
        { provide: ReservationService, useValue: reservationServiceSpy },
        { provide: RoomsService, useValue: roomsServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReservationFormComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);

    fixture.detectChanges(); //ngOnInit
  });

  it('You must create the component', () => {
    expect(component).toBeTruthy();
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
    expect(window.alert).toHaveBeenCalledWith(
      jasmine.stringMatching(/success/),
    );
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('onSubmit: should handle error from service', () => {
    reservationServiceSpy.createReservation.and.returnValue(
      throwError(() => ({ error: 'Room occupied' })),
    );

    spyOn(window, 'alert');

    component.roomId = 1;
    component.selectedDate = '2026-05-20';
    component.selectedStartTime = '10:00';
    component.selectedEndTime = '11:00';

    component.onSubmit();

    expect(window.alert).toHaveBeenCalledWith('Room occupied');
  });

  it('calculateStartTimes: should generate slots from 08:00 to 20:30', () => {
    component.selectedDate = '2050-01-01';
    component.minDate = '2025-01-01';
    component.occupiedSlots = [];

    component.calculateStartTimes();

    expect(component.startTimes.length).toBeGreaterThan(0);
    expect(component.startTimes[0]).toBe('08:00');
    expect(component.startTimes[component.startTimes.length - 1]).toBe('20:30');
  });

  it('calculateStartTimes: should filter out occupied slots', () => {
    component.selectedDate = '2050-01-01';
    component.occupiedSlots = [
      {
        startDate: '2050-01-01T10:00:00',
        endDate: '2050-01-01T11:00:00',
      },
    ];

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
    component.occupiedSlots = [
      {
        startDate: '2026-01-01T10:00:00',
        endDate: '2026-01-01T11:00:00',
      },
    ];

    component.onStartTimeChange();

    const endTimes = component.endTimes;
    expect(endTimes).toContain('10:00');
    expect(endTimes).not.toContain('10:30');
  });

  it('onConfigChange: should call checkAvailability', () => {
    reservationServiceSpy.checkAvailability.and.returnValue(of([]));

    component.roomId = 1;
    component.selectedDate = '2026-05-05';
    component.onConfigChange();

    expect(reservationServiceSpy.checkAvailability).toHaveBeenCalledWith(
      1,
      '2026-05-05',
    );
  });

  it('ngOnInit: should handle error when loading rooms', () => {
    spyOn(console, 'error');
    roomsServiceSpy.searchRooms.and.returnValue(
      throwError(() => new Error('Load error')),
    );

    component.ngOnInit();

    expect(console.error).toHaveBeenCalled();
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
    component.selectedDate = '2050-01-01';
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

    roomsServiceSpy.getRooms.and.returnValue(
      of({ content: [], totalElements: 0, sort: [] } as any),
    );

    component.ngOnInit();

    expect(component.rooms.length).toBe(0);
    expect(component.roomId).toBeNull();
  });

  it('onConfigChange: should handle error from checkAvailability', () => {
    spyOn(console, 'error');
    reservationServiceSpy.checkAvailability.and.returnValue(
      throwError(() => new Error('Network fail')),
    );

    component.roomId = 1;
    component.selectedDate = '2026-01-01';

    component.onConfigChange();

    expect(console.error).toHaveBeenCalledWith(
      'Error checking availability',
      jasmine.any(Error),
    );
  });

  it('You must upload the list of classrooms', () => {
    component.searchRooms();
    fixture.detectChanges();

    expect(component.visibleRooms.length).toBe(3);
    expect(component.roomId).toBeNull();
  });

  it('should clear room search and trigger searchRooms', () => {
    spyOn(component, 'searchRooms');
    component.roomSearchText = 'Java';
    component.selectedCampus = 'MOSTOLES';
    component.minCapacity = 20;

    component.clearRoomSearch();

    expect(component.roomSearchText).toBe('');
    expect(component.selectedCampus).toBe('');
    expect(component.minCapacity).toBeNull();
    expect(component.searchRooms).toHaveBeenCalled();
  });

  it('generateAllPossibleTimes: should populate allPossibleTimes with 30-min intervals', () => {
    component.allPossibleTimes = [];
    component.generateAllPossibleTimes();

    expect(component.allPossibleTimes.length).toBeGreaterThan(0);
    expect(component.allPossibleTimes).toContain('08:00');
    expect(component.allPossibleTimes).toContain('14:30');
    expect(component.allPossibleTimes).toContain('21:00');
  });

  it('triggerSmartSearch: should return early if dates/times are missing', () => {
    component.selectedDate = '';
    component.triggerSmartSearch();
    expect(reservationServiceSpy.smartSearch).not.toHaveBeenCalled();
  });

  it('triggerSmartSearch: should call service and populate suggestions on success', () => {
    component.selectedDate = '2026-05-05';
    component.desiredStartTime = '10:00';
    component.desiredEndTime = '11:00';
    component.roomId = 1;
    component.visibleRooms = [{ id: 1, camp: 'MOSTOLES' } as any];

    const mockSuggestions = [
      {
        room: { id: 2, name: 'Room 2' },
        suggestedStart: '2026-05-05T10:30:00',
        suggestedEnd: '2026-05-05T11:30:00',
        reason: 'ALTERNATIVE_TIME',
      },
    ];
    reservationServiceSpy.smartSearch.and.returnValue(of(mockSuggestions));

    component.triggerSmartSearch();

    expect(reservationServiceSpy.smartSearch).toHaveBeenCalled();
    expect(component.smartSuggestions).toEqual(mockSuggestions);
    expect(component.smartSearchLoading).toBeFalse();
  });

  it('triggerSmartSearch: should show alert if no suggestions found', () => {
    spyOn(window, 'alert');
    component.selectedDate = '2026-05-05';
    component.desiredStartTime = '10:00';
    component.desiredEndTime = '11:00';

    reservationServiceSpy.smartSearch.and.returnValue(of([]));

    component.triggerSmartSearch();

    expect(window.alert).toHaveBeenCalledWith(
      jasmine.stringMatching(/No alternatives found/i),
    );
    expect(component.smartSearchLoading).toBeFalse();
  });

  it('triggerSmartSearch: should handle error from service', () => {
    spyOn(console, 'error');
    component.selectedDate = '2026-05-05';
    component.desiredStartTime = '10:00';
    component.desiredEndTime = '11:00';

    reservationServiceSpy.smartSearch.and.returnValue(
      throwError(() => new Error('Search failed')),
    );

    component.triggerSmartSearch();

    expect(console.error).toHaveBeenCalled();
    expect(component.smartSearchLoading).toBeFalse();
  });

  it('applySuggestion: should auto-fill form and clear search panel', fakeAsync(() => {
    spyOn(component, 'onConfigChange');
    spyOn(component, 'onStartTimeChange');

    const startObj = new Date();
    startObj.setFullYear(2026, 4, 5);
    startObj.setHours(10, 30, 0, 0);

    const endObj = new Date();
    endObj.setFullYear(2026, 4, 5);
    endObj.setHours(12, 0, 0, 0);

    const suggestion = {
      room: { id: 5 },
      suggestedStart: startObj.toISOString(),
      suggestedEnd: endObj.toISOString(),
    };

    component.applySuggestion(suggestion);

    expect(component.roomId).toBe(5);
    expect(component.selectedDate).toBe(startObj.toISOString().split('T')[0]);
    expect(component.onConfigChange).toHaveBeenCalled();

    tick(400);

    expect(component.selectedStartTime).toBe('10:30');
    expect(component.onStartTimeChange).toHaveBeenCalled();
    expect(component.selectedEndTime).toBe('12:00');

    expect(component.smartSuggestions.length).toBe(0);
    expect(component.desiredStartTime).toBe('');
    expect(component.desiredEndTime).toBe('');
  }));
});
