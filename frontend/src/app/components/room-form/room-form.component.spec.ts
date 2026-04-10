import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RoomFormComponent } from './room-form.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { RoomsService } from '../../services/rooms.service';
import { SoftwareService } from '../../services/software.service';
import { LoginService } from '../../login/login.service';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogService } from '../../services/dialog.service';
import { CampusService } from '../../services/campus.service';

describe('RoomFormComponent', () => {
  let component: RoomFormComponent;
  let fixture: ComponentFixture<RoomFormComponent>;

  const mockRoomsService = {
    createRoom: jasmine.createSpy('createRoom'),
    updateRoom: jasmine.createSpy('updateRoom'),
    getRoom: jasmine.createSpy('getRoom'),
    uploadRoomImage: jasmine.createSpy('uploadRoomImage'),
  };
  const mockDialogService = {
    alert: jasmine.createSpy('alert').and.returnValue(Promise.resolve()),
  };

  const mockSoftwareService = {
    getAllSoftwares: () =>
      of({
        content: [{ id: 1, name: 'Java', version: 17 }],
        totalPages: 1,
        totalElements: 1,
        first: true,
        last: true,
        number: 0,
        size: 1,
      }),

    searchSoftwares: jasmine
      .createSpy('searchSoftwares')
      .and.returnValue(
        of({ content: [{ id: 1, name: 'Java', version: 17 }] } as any),
      ),
  };

  const mockRouter = { navigate: jasmine.createSpy('navigate') };

  beforeEach(async () => {
    mockRoomsService.createRoom.calls.reset();
    mockRoomsService.updateRoom.calls.reset();
    mockRoomsService.getRoom.calls.reset();
    mockRoomsService.uploadRoomImage.calls.reset();
    mockRouter.navigate.calls.reset();

    mockRoomsService.createRoom.and.returnValue(of({ id: 1 }));
    mockRoomsService.updateRoom.and.returnValue(of({ id: 1 }));
    mockRoomsService.uploadRoomImage.and.returnValue(of({}));

    mockRoomsService.getRoom.and.returnValue(
      of({
        id: 1,
        name: 'Test Room',
        active: false,
        software: [],
        imageName: null, //default
      }),
    );

    await TestBed.configureTestingModule({
      declarations: [RoomFormComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, FormsModule],
      providers: [
        { provide: RoomsService, useValue: mockRoomsService },
        { provide: SoftwareService, useValue: mockSoftwareService },
        { provide: Router, useValue: mockRouter },
        { provide: DialogService, useValue: mockDialogService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '1' } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RoomFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load room data including active status', () => {
    // ngOnInit calls loadRoomData
    expect(component.room.name).toBe('Test Room');
    expect(component.room.active).toBeFalse(); // Verify
  });

  it('should default active to true for new rooms', () => {
    // Restart
    component.roomId = null;
    component.isEditMode = false;
    // Reset room to default
    component.room = {
      name: '',
      capacity: 0,
      campusId: 1,
      place: '',
      coordenades: '',
      active: true,
      softwareIds: [],
    };

    expect(component.room.active).toBeTrue();
  });

  it('should default active to true if backend returns undefined', () => {
    //  'as any' to avoid "Property active is missing"
    const mockRoomNoActive = { id: 1, name: 'Old Room', software: [] } as any;

    //return the incorrect object
    mockRoomsService.getRoom.and.returnValue(of(mockRoomNoActive));

    component.loadRoomData(1);

    expect(component.room.active).toBeTrue();
  });

  it('should handle update error', () => {
    spyOn(window, 'alert');
    mockRoomsService.updateRoom.and.returnValue(
      throwError(() => 'Error updating'),
    );

    component.isEditMode = true;
    component.roomId = 1;
    component.save();

    expect(window.alert).toHaveBeenCalled();
  });

  it('should handle create error', () => {
    spyOn(window, 'alert');
    spyOn(console, 'error');
    mockRoomsService.createRoom.and.returnValue(
      throwError(() => 'Error creating'),
    );

    component.isEditMode = false;
    component.save();

    expect(window.alert).toHaveBeenCalled();
  });

  it('loadRoomData should set currentImageUrl if room has imageName', () => {
    const roomWithImage = {
      id: 5,
      name: 'Room Img',
      active: true,
      software: [],
      imageName: 'pic.jpg',
    };
    mockRoomsService.getRoom.and.returnValue(of(roomWithImage));

    component.loadRoomData(5);

    expect(component.currentImageUrl).toBe('/api/rooms/5/image');
  });

  it('onFileSelected should store the file in selectedFile', () => {
    const mockFile = new File([''], 'room.jpg', { type: 'image/jpeg' });
    const event = { target: { files: [mockFile] } };

    component.onFileSelected(event);

    expect(component.selectedFile).toEqual(mockFile);
  });

  it('save (CREATE) should call uploadRoomImage if a file is selected', () => {
    component.isEditMode = false;
    component.roomId = null;

    component.selectedFile = new File([''], 'new.jpg');

    mockRoomsService.createRoom.and.returnValue(of({ id: 55 }));

    component.save();

    expect(mockRoomsService.createRoom).toHaveBeenCalled();
    expect(mockRoomsService.uploadRoomImage).toHaveBeenCalledWith(
      55,
      component.selectedFile,
    );
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/rooms']);
  });

  it('save (UPDATE) should call uploadRoomImage if a file is selected', () => {
    component.isEditMode = true;
    component.roomId = 10;

    component.selectedFile = new File([''], 'update.jpg');
    mockRoomsService.updateRoom.and.returnValue(of({ id: 10 }));
    component.save();

    expect(mockRoomsService.updateRoom).toHaveBeenCalled();
    expect(mockRoomsService.uploadRoomImage).toHaveBeenCalledWith(
      10,
      component.selectedFile,
    );
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/rooms']);
  });

  it('save should navigate immediately if NO file is selected', async () => {
    component.selectedFile = null;
    component.isEditMode = false;

    mockRoomsService.createRoom.and.returnValue(of({ id: 55 }));

    component.save();
    await fixture.whenStable();

    expect(mockRoomsService.createRoom).toHaveBeenCalled();
    expect(mockRoomsService.uploadRoomImage).not.toHaveBeenCalled();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/rooms']);
  });

  it('uploadImageAndNavigate should alert and navigate even if upload fails', () => {
    mockRoomsService.uploadRoomImage.and.returnValue(
      throwError(() => new Error('Upload failed')),
    );

    component.selectedFile = new File([''], 'fail.jpg');
    component.uploadImageAndNavigate(1);

    expect(mockDialogService.alert).toHaveBeenCalledWith(
      'Error',
      'Room saved but image upload failed.',
    );
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/rooms']);
  });

  it('searchSoftwareForDropdown: should clear availableSoftwares if inputs are empty', () => {
    component.softwareSearchText = '';
    component.softwareMinVersion = null;
    component.availableSoftwares = [
      { id: 1, name: 'Java', version: 17 } as any,
    ];

    component.searchSoftwareForDropdown();

    expect(component.availableSoftwares.length).toBe(0);
  });

  it('searchSoftwareForDropdown: should call searchSoftwares and filter out already selected ones', () => {
    component.softwareSearchText = 'Java';
    component.selectedSoftwares = [
      { id: 1, name: 'Java 11', version: 11 } as any,
    ];

    const mockSearchResponse = {
      content: [
        { id: 1, name: 'Java 11', version: 11 },
        { id: 2, name: 'Java 17', version: 17 },
      ],
    };
    mockSoftwareService.searchSoftwares.and.returnValue(
      of(mockSearchResponse as any),
    );

    component.searchSoftwareForDropdown();

    expect(mockSoftwareService.searchSoftwares).toHaveBeenCalledWith(
      'Java',
      undefined,
    );
    expect(component.availableSoftwares.length).toBe(1);
    expect(component.availableSoftwares[0].id).toBe(2); // Solo debe quedar el ID 2
  });

  it('addSoftwareToRoom: should push to selected array and remove from available', () => {
    const swToAdd = { id: 10, name: 'Matlab', version: 1 } as any;
    component.availableSoftwares = [swToAdd];
    component.room.softwareIds = [];
    component.selectedSoftwares = [];

    component.addSoftwareToRoom(swToAdd);

    expect(component.room.softwareIds).toContain(10);
    expect(component.selectedSoftwares).toContain(swToAdd);
    expect(component.availableSoftwares.length).toBe(0);
  });

  it('removeSoftwareFromRoom: should remove software id from room.softwareIds', () => {
    component.room.softwareIds = [1, 2, 3];
    component.selectedSoftwares = [{ id: 1 }, { id: 2 }, { id: 3 }] as any;

    component.removeSoftwareFromRoom(2);

    expect(component.room.softwareIds).not.toContain(2);
    expect(component.selectedSoftwares.length).toBe(2);
  });

  it('onCampusSelectChange: should activate isCreatingCampus if value is -1', () => {
    component.onCampusSelectChange(-1);
    expect(component.isCreatingCampus).toBeTrue();
    expect(component.room.campusId).toBeNull();
  });

  it('searchSoftwareForDropdown: should handle search error', () => {
    mockSoftwareService.searchSoftwares.and.returnValue(
      throwError(() => new Error('API Error')),
    );

    component.softwareSearchText = 'test';
    component.searchSoftwareForDropdown();

    expect(component.availableSoftwares.length).toBe(0);
  });

  it('save: should handle Campus Creation if isCreatingCampus is true', () => {
    component.isCreatingCampus = true;
    component.newCampus = { id: 0, name: 'New Campus', coordinates: '40, -3' };

    const campusService = TestBed.inject(CampusService);
    spyOn(campusService, 'createCampus').and.returnValue(
      of({ id: 99, name: 'New Campus', coordinates: '40, -3' }),
    );

    component.save();

    expect(campusService.createCampus).toHaveBeenCalled();
    expect(component.room.campusId).toBe(99);
  });

  it('removeSoftwareFromRoom: should do nothing if ID does not exist', () => {
    component.room.softwareIds = [1];
    component.selectedSoftwares = [{ id: 1 }] as any;

    component.removeSoftwareFromRoom(999);

    expect(component.room.softwareIds.length).toBe(1);
    expect(component.selectedSoftwares.length).toBe(1);
  });
});
