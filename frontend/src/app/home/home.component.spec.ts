import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeComponent } from './home.component';
import { RoomsService } from '../services/rooms.service';
import { LoginService } from '../login/login.service';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';


describe('HomeComponent UI Test', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let mockRoomsService: any;
  let mockLoginService: any;

  // Datos de prueba
  const mockRooms = [
    { id: 1, name: 'Aula Magna', capacity: 100, camp: 'MOSTOLES', place: 'Aulario I', software: [] },
    { id: 2, name: 'Laboratorio 1', capacity: 20, camp: 'ALCORCON', place: 'Lab II', software: [] }
  ];

  beforeEach(async () => {
    mockRoomsService = {
      getRooms: jasmine.createSpy('getRooms').and.returnValue(of(mockRooms))
    };

    mockLoginService = {
      isLogged: () => true, //to see the button we have to be log in first
      isAdmin: () => false
    };

    await TestBed.configureTestingModule({
      declarations: [ HomeComponent ],
      imports: [ RouterTestingModule ],
      providers: [
        { provide: RoomsService, useValue: mockRoomsService },
        { provide: LoginService, useValue: mockLoginService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('Debe mostrar el título "Available Rooms"', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h3')?.textContent).toContain('Available Rooms');
  });

  it('Debe renderizar una tarjeta por cada aula (2 aulas)', () => {
    // Buscamos todos los elementos con clase .card
    const cards = fixture.debugElement.queryAll(By.css('.card'));
    expect(cards.length).toBe(2);
  });

  it('Debe mostrar el nombre del aula "Aula Magna" en la primera tarjeta', () => {
    const firstCardTitle = fixture.nativeElement.querySelector('.card-title');
    expect(firstCardTitle.textContent).toContain('Aula Magna');
  });
});

/*describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [HomeComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});*/
