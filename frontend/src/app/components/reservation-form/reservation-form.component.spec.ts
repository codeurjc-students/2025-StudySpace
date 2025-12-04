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
      { id: 1, name: 'Aula Test 1', capacity: 20 },
      { id: 2, name: 'Aula Test 2', capacity: 30 }
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

  it('Debe crear el componente', () => {
    expect(component).toBeTruthy();
  });

  it('Debe cargar la lista de aulas', () => {
    expect(component.rooms.length).toBe(2);
  });

  it('Debe tener el botón "Confirmar Reserva" deshabilitado al inicio', async () => {
    // 1. Aseguramos que roomId es nulo
    component.roomId = null;
    
    // 2. Forzamos la detección de cambios y esperamos a que el formulario (ngModel) reaccione
    fixture.detectChanges();
    await fixture.whenStable(); 
    fixture.detectChanges(); // Segunda pasada para pintar el estado final

    // 3. Buscamos el botón
    const button = fixture.nativeElement.querySelector('button[type="submit"]');
    
    // 4. Verificamos la propiedad 'disabled' del elemento HTML
    expect(button.disabled).toBeTrue();
  });
});



/*describe('ReservationFormComponent UI Test', () => {
  let component: ReservationFormComponent;
  let fixture: ComponentFixture<ReservationFormComponent>;

  // Mock simple del servicio de salas
  const mockRoomsService = {
    getRooms: () => of([
      { id: 1, name: 'Aula Test 1', capacity: 20 },
      { id: 2, name: 'Aula Test 2', capacity: 30 }
    ])
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReservationFormComponent ],
      imports: [
        HttpClientTestingModule, // Simula peticiones HTTP
        RouterTestingModule,     // Simula rutas
        FormsModule              // Simula formularios
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

  it('Debe crear el componente de reserva', () => {
    expect(component).toBeTruthy();
  });

  it('Debe cargar la lista de aulas en el desplegable', () => {
    // Verificamos que la variable rooms tiene 2 elementos (del mock)
    expect(component.rooms.length).toBe(2);
    expect(component.rooms[0].name).toBe('Aula Test 1');
  });

  it('Debe tener el botón "Confirmar Reserva" deshabilitado si no hay aula seleccionada', () => {
    // 1. Forzamos la situación que queremos probar
    component.roomId = null; 
    
    // 2. Pedimos a Angular que actualice el HTML con este cambio
    fixture.detectChanges(); 
    
    // 3. Ahora comprobamos el botón
    const button = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(button.disabled).toBeTrue();
  });
});*/

/*describe('ReservationFormComponent', () => {
  let component: ReservationFormComponent;
  let fixture: ComponentFixture<ReservationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReservationFormComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ReservationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});*/
