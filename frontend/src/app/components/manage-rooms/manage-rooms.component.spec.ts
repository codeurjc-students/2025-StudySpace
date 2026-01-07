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
    
    roomsServiceSpy.getRooms.and.returnValue(of(mockPage as any));
    
    fixture.detectChanges();
  });

  it('should create and load rooms on init', () => {
    expect(component).toBeTruthy();
    expect(roomsServiceSpy.getRooms).toHaveBeenCalledWith(0);
    expect(component.rooms.length).toBe(1);
  });

  it('should handle error when loading rooms', () => {
    spyOn(console, 'error');
    roomsServiceSpy.getRooms.and.returnValue(throwError(() => new Error('Error loading')));
    
    component.loadRooms(1);
    
    expect(console.error).toHaveBeenCalled();
  });

  it('should delete room if confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    roomsServiceSpy.deleteRoom.and.returnValue(of({}));

    spyOn(component, 'loadRooms'); 

    component.rooms = [{ 
        id: 1, 
        name: 'To Delete', 
        capacity: 10, 
        camp: 'Móstoles', 
        place: '', 
        active: true, 
        coordenades: '', 
        software: [] 
    }];
    
    component.deleteRoom(1);

    expect(roomsServiceSpy.deleteRoom).toHaveBeenCalledWith(1);
    expect(component.rooms.length).toBe(0); 
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/successfully/i));
    

    expect(component.loadRooms).toHaveBeenCalledWith(component.currentPage);
  });

  it('should NOT delete room if cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteRoom(1);
    expect(roomsServiceSpy.deleteRoom).not.toHaveBeenCalled();
  });

  it('should handle error on delete', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    spyOn(console, 'error');
    roomsServiceSpy.deleteRoom.and.returnValue(throwError(() => new Error('Delete failed')));

    component.deleteRoom(1);

    expect(console.error).toHaveBeenCalled();
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/Error/i));
  });
  
  it('pagination logic', () => {
    component.pageData = { totalPages: 5 } as any;
    expect(component.getVisiblePages()).toEqual([0, 1, 2, 3, 4]);
    
    component.pageData = undefined;
    expect(component.getVisiblePages()).toEqual([]);
  });
});