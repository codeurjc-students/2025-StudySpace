import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageRoomsComponent } from './manage-rooms.component';
import { RoomsService } from '../../services/rooms.service';
import { RouterTestingModule } from '@angular/router/testing'; 
import { of } from 'rxjs';

describe('ManageRoomsComponent', () => {
  let component: ManageRoomsComponent;
  let fixture: ComponentFixture<ManageRoomsComponent>;

  // Mock for service
  const mockRoomsService = {
    getRooms: () => of([
      { id: 1, name: 'Aula 1', capacity: 20, camp: 'MOSTOLES' },
      { id: 2, name: 'Aula 2', capacity: 30, camp: 'ALCORCON' }
    ]),
    deleteRoom: (id: number) => of({}) 
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ManageRoomsComponent ],
      imports: [ RouterTestingModule ], 
      providers: [
        { provide: RoomsService, useValue: mockRoomsService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageRoomsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load rooms on init', () => {
    expect(component.rooms.length).toBe(2);
    expect(component.rooms[0].name).toBe('Aula 1');
  });
});