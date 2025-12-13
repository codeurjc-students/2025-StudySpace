import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReservationFormComponent } from './reservation-form.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms'; // for [(ngModel)]
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { LoginService } from '../../login/login.service';
import { of } from 'rxjs';




describe('ReservationFormComponent UI Test', () => {
  let component: ReservationFormComponent;
  let fixture: ComponentFixture<ReservationFormComponent>;

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
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        FormsModule
      ],
      providers: [
        ReservationService,
        LoginService,
        { provide: RoomsService, useValue: mockRoomsService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReservationFormComponent);
    component = fixture.componentInstance;
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
});



