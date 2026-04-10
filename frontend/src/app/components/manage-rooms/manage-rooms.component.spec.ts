import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { ManageRoomsComponent } from './manage-rooms.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { RoomsService } from '../../services/rooms.service';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RoomDTO } from '../../dtos/room.dto';
import { PaginationComponent } from '../pagination/pagination.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { DialogService } from '../../services/dialog.service';
import { CampusService } from '../../services/campus.service';

describe('ManageRoomsComponent', () => {
  let component: ManageRoomsComponent;
  let fixture: ComponentFixture<ManageRoomsComponent>;
  let roomsServiceSpy: jasmine.SpyObj<RoomsService>;
  let dialogServiceSpy: jasmine.SpyObj<DialogService>;
  let campusServiceSpy: jasmine.SpyObj<CampusService>;

  const mockRoom: RoomDTO = {
    id: 1,
    name: 'Lab 1',
    capacity: 20,
    campus: { id: 1, name: 'Móstoles', coordinates: '0,0' },
    place: 'Edificio 1',
    coordenades: '',
    active: true,
    software: [],
  };

  const mockPage = {
    content: [mockRoom],
    totalPages: 3,
    number: 0,
    size: 10,
  };

  beforeEach(async () => {
    roomsServiceSpy = jasmine.createSpyObj('RoomsService', [
      'getRooms',
      'deleteRoom',
      'searchRooms',
    ]);
    campusServiceSpy = jasmine.createSpyObj('CampusService', [
      'getAllCampus',
      'createCampus',
      'updateCampus',
      'deleteCampus',
    ]);
    dialogServiceSpy = jasmine.createSpyObj('DialogService', [
      'alert',
      'prompt',
    ]);

    campusServiceSpy.getAllCampus.and.returnValue(of([]));
    dialogServiceSpy.alert.and.returnValue(Promise.resolve());
    dialogServiceSpy.prompt.and.returnValue(
      Promise.resolve('Reason for deletion'),
    );

    await TestBed.configureTestingModule({
      declarations: [ManageRoomsComponent, PaginationComponent],
      imports: [HttpClientTestingModule, FormsModule, RouterTestingModule],
      providers: [
        { provide: RoomsService, useValue: roomsServiceSpy },
        { provide: DialogService, useValue: dialogServiceSpy },
        { provide: CampusService, useValue: campusServiceSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ManageRoomsComponent);
    component = fixture.componentInstance;

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
    roomsServiceSpy.getRooms.and.returnValue(
      throwError(() => new Error('Error loading')),
    );
    component.loadRooms(1);
    expect(console.error).toHaveBeenCalled();
  });

  it('should delete room if reason is provided via prompt', fakeAsync(() => {
    const reason = 'Reason for deletion';
    dialogServiceSpy.prompt.and.returnValue(Promise.resolve(reason));
    roomsServiceSpy.deleteRoom.and.returnValue(of({}));
    const loadSpy = spyOn(component, 'loadRooms');

    component.deleteRoom(1);

    tick(); // prompt
    tick(); //alert and delete
    tick(); // alert and loadRooms

    expect(roomsServiceSpy.deleteRoom).toHaveBeenCalledWith(1, reason);
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Success',
      jasmine.stringMatching(/successfully/i),
    );
    expect(loadSpy).toHaveBeenCalled();
  }));

  it('should NOT delete room if prompt is cancelled (null)', async () => {
    dialogServiceSpy.prompt.and.returnValue(Promise.resolve(null));
    component.deleteRoom(1);
    await fixture.whenStable();
    expect(roomsServiceSpy.deleteRoom).not.toHaveBeenCalled();
  });

  it('should handle error on delete', async () => {
    dialogServiceSpy.prompt.and.returnValue(Promise.resolve('Reason'));
    roomsServiceSpy.deleteRoom.and.returnValue(
      throwError(() => new Error('Delete failed')),
    );

    component.deleteRoom(1);
    await fixture.whenStable();

    expect(console.error).toHaveBeenCalled();
  });

  it('pagination logic should return correct visible pages', () => {
    component.pageData = { totalPages: 5 } as any;
    expect(component.getVisiblePages()).toEqual([0, 1, 2, 3, 4]);

    component.pageData = undefined;
    expect(component.getVisiblePages()).toEqual([]);
  });

  it('onSearch: should call clearSearch if all fields are empty', () => {
    spyOn(component, 'clearSearch');
    component.searchText = '';
    component.selectedCampusId = null;
    component.minCapacity = null;
    component.filterActive = '';

    component.onSearch();

    expect(component.clearSearch).toHaveBeenCalled();
  });

  it('onSearch: should activate search mode and load first page', () => {
    spyOn(component, 'loadRooms');
    component.searchText = 'Lab';

    component.onSearch();

    expect(component.isSearching).toBeTrue();
    expect(component.loadRooms).toHaveBeenCalledWith(0);
  });

  it('clearSearch: should reset fields and reload normal data', () => {
    spyOn(component, 'loadRooms');
    component.searchText = 'Java';
    component.selectedCampusId = 1;
    component.minCapacity = 20;
    component.filterActive = 'true';
    component.isSearching = true;

    component.clearSearch();

    expect(component.searchText).toBe('');
    expect(component.selectedCampusId).toBeNull();
    expect(component.minCapacity).toBeNull();
    expect(component.filterActive).toBe('');
    expect(component.isSearching).toBeFalse();
    expect(component.loadRooms).toHaveBeenCalledWith(0);
  });

  it('loadRooms: should use searchRooms with correct filters when isSearching is true', () => {
    component.isSearching = true;
    component.searchText = 'Lab';
    component.filterActive = 'true';

    roomsServiceSpy.searchRooms.and.returnValue(of(mockPage as any));

    component.loadRooms(0);

    expect(roomsServiceSpy.searchRooms).toHaveBeenCalledWith(
      'Lab',
      undefined,
      undefined,
      true,
      0,
    );
    expect(component.rooms.length).toBeGreaterThan(0);
  });
});
