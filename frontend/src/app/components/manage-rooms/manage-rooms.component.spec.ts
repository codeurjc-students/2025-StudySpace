import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageRoomsComponent } from './manage-rooms.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { RoomsService } from '../../services/rooms.service';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RoomDTO } from '../../dtos/room.dto';
import { PaginationComponent } from '../pagination/pagination.component';

describe('ManageRoomsComponent', () => {
  let component: ManageRoomsComponent;
  let fixture: ComponentFixture<ManageRoomsComponent>;
  let roomsServiceSpy: jasmine.SpyObj<RoomsService>;

  const mockRoom: RoomDTO = { 
    id: 1, 
    name: 'Lab 1', 
    capacity: 20, 
    camp: 'Móstoles', 
    place: 'Edificio 1', 
    coordenades: '', 
    active: true, 
    software: [] 
  };

  const mockPage = {
    content: [mockRoom],
    totalPages: 3,
    number: 0,
    size: 10
  };

  beforeEach(async () => {
    roomsServiceSpy = jasmine.createSpyObj('RoomsService', ['getRooms', 'deleteRoom']);

    await TestBed.configureTestingModule({
      declarations: [ManageRoomsComponent, PaginationComponent],
      imports: [
        HttpClientTestingModule, 
        FormsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: RoomsService, useValue: roomsServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ManageRoomsComponent);
    component = fixture.componentInstance;
    
    // Espías globales para evitar errores de duplicidad
    spyOn(window, 'alert');
    spyOn(window, 'prompt');
    spyOn(console, 'error');
    
    roomsServiceSpy.getRooms.and.returnValue(of(mockPage as any));
    
    fixture.detectChanges();
  });

  it('should create and load rooms on init', () => {
    expect(component).toBeTruthy();
    expect(roomsServiceSpy.getRooms).toHaveBeenCalledWith(0);
    expect(component.rooms.length).toBe(1);
  });

  it('should handle error when loading rooms', () => {
    roomsServiceSpy.getRooms.and.returnValue(throwError(() => new Error('Error loading')));
    component.loadRooms(1);
    expect(console.error).toHaveBeenCalled();
  });

  it('should delete room if reason is provided via prompt', () => {
    const reason = 'Reason for deletion';
    (window.prompt as jasmine.Spy).and.returnValue(reason);
    roomsServiceSpy.deleteRoom.and.returnValue(of({}));
    const loadSpy = spyOn(component, 'loadRooms');

    component.deleteRoom(1);

    expect(roomsServiceSpy.deleteRoom).toHaveBeenCalledWith(1, reason);
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/successfully/i));
    expect(loadSpy).toHaveBeenCalled();
  });

  it('should NOT delete room if prompt is cancelled (null)', () => {
    (window.prompt as jasmine.Spy).and.returnValue(null);
    component.deleteRoom(1);
    expect(roomsServiceSpy.deleteRoom).not.toHaveBeenCalled();
  });

  it('should handle error on delete', () => {
    (window.prompt as jasmine.Spy).and.returnValue('Reason');
    roomsServiceSpy.deleteRoom.and.returnValue(throwError(() => new Error('Delete failed')));

    component.deleteRoom(1);

    expect(console.error).toHaveBeenCalled();
  });
  
  it('pagination logic should return correct visible pages', () => {
    component.pageData = { totalPages: 5 } as any;
    expect(component.getVisiblePages()).toEqual([0, 1, 2, 3, 4]);
    
    component.pageData = undefined;
    expect(component.getVisiblePages()).toEqual([]);
  });
});