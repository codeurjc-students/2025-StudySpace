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
  let reservationService: ReservationService; 
  let router: Router;

  const mockRoomsService = {
    getRooms: () => of([
      { id: 1, name: 'Aula Test 1', capacity: 20, camp: 'MOSTOLES', active: true },
      { id: 2, name: 'Aula Test 2', capacity: 30, camp: 'ALCORCON', active: true },
      { id: 3, name: 'Aula Desactivada', capacity: 15, camp: 'VICALVARO', active: false }
    ])
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReservationFormComponent ],
      imports: [ HttpClientTestingModule, RouterTestingModule, FormsModule ],
      providers: [
        ReservationService,
        LoginService,
        { provide: RoomsService, useValue: mockRoomsService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReservationFormComponent);
    component = fixture.componentInstance;
    
    // Inyección de servicios para que estén disponibles en los tests
    reservationService = TestBed.inject(ReservationService);
    router = TestBed.inject(Router);
    
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
    component.startDate = '';
    component.onSubmit();
    expect(window.alert).toHaveBeenCalledWith('Please fill in the dates.');
  });

  it('onSubmit: should call service and navigate on success', () => {
    spyOn(reservationService, 'createReservation').and.returnValue(of({}));
    spyOn(router, 'navigate');
    spyOn(window, 'alert');

    component.roomId = 1;
    component.startDate = '2025-01-01T10:00';
    component.endDate = '2025-01-01T12:00';
    
    component.onSubmit();

    expect(reservationService.createReservation).toHaveBeenCalled();
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/success/));
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('onSubmit: should handle error from service', () => {
    spyOn(reservationService, 'createReservation').and.returnValue(throwError(() => new Error('API Error')));
    spyOn(window, 'alert');

    component.roomId = 1;
    component.startDate = '2025-01-01T10:00';
    component.endDate = '2025-01-01T12:00';
    
    component.onSubmit();

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/Error/));
  });





});



