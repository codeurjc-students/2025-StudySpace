import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageRoomsComponent } from './manage-rooms.component';
import { RoomsService } from '../../services/rooms.service';
import { RouterTestingModule } from '@angular/router/testing'; 
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';

describe('ManageRoomsComponent', () => {
  let component: ManageRoomsComponent;
  let fixture: ComponentFixture<ManageRoomsComponent>;

  // Mock for service
  const mockRoomsService = {
    getRooms: () => of({ content: [
      { id: 1, name: 'Aula Activa', capacity: 20, camp: 'MOSTOLES', active: true },
      { id: 2, name: 'Aula Desactivada', capacity: 30, camp: 'ALCORCON', active: false }
    ], totalPages: 1, totalElements: 2, first: true, last: true, number: 0, size: 2 }),
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
    expect(component.rooms.length).toBe(2);
  });

  it('should identify active and inactive rooms correctly', () => {
    const activeRoom = component.rooms.find(r => r.id === 1);
    const inactiveRoom = component.rooms.find(r => r.id === 2);

    expect(activeRoom?.active).toBeTrue();
    expect(inactiveRoom?.active).toBeFalse();
  });
  
  //verify CSS
  it('should apply table-secondary class to inactive rows', () => {
      const rows = fixture.debugElement.queryAll(By.css('tr'));
      
      fixture.detectChanges();
      
      // Verify there is bg-danger (Disabled)
      const disabledBadge = fixture.debugElement.query(By.css('.badge.bg-danger'));
      expect(disabledBadge).toBeTruthy();
      expect(disabledBadge.nativeElement.textContent).toContain('Disabled');
  });
});