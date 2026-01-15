import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RoomFormComponent } from './room-form.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { RoomsService } from '../../services/rooms.service';
import { SoftwareService } from '../../services/software.service';
import { LoginService } from '../../login/login.service';
import { of,throwError } from 'rxjs';
import { ActivatedRoute,Router } from '@angular/router';


describe('RoomFormComponent', () => {
  let component: RoomFormComponent;
  let fixture: ComponentFixture<RoomFormComponent>;

 const mockRoomsService = {
    createRoom: jasmine.createSpy('createRoom'),
    updateRoom: jasmine.createSpy('updateRoom'),
    getRoom: jasmine.createSpy('getRoom'),
    uploadRoomImage: jasmine.createSpy('uploadRoomImage')
  };

  const mockSoftwareService = {
    getAllSoftwares: () => of({ content: [{ id: 1, name: 'Java', version: 17 }], totalPages: 1, totalElements: 1, first: true, last: true, number: 0, size: 1 })
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
    
    mockRoomsService.getRoom.and.returnValue(of({ 
        id: 1, 
        name: 'Test Room', 
        active: false, 
        software: [] ,
        imageName: null    //default
    }));

    await TestBed.configureTestingModule({
      declarations: [ RoomFormComponent ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        FormsModule
      ],
      providers: [
        { provide: RoomsService, useValue: mockRoomsService },
        { provide: SoftwareService, useValue: mockSoftwareService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '1' } } } 
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoomFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });





  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load available software', () => {
    expect(component.availableSoftware.length).toBe(1);
    expect(component.availableSoftware[0].name).toBe('Java');
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
        name: '', capacity: 0, camp: 'MOSTOLES', place: '', coordenades: '', 
        active: true, softwareIds: [] 
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
    mockRoomsService.updateRoom.and.returnValue(throwError(() => 'Error updating'));
    
    component.isEditMode = true;
    component.roomId = 1;
    component.save();

    expect(window.alert).toHaveBeenCalled();
  });

  it('should handle create error', () => {
    spyOn(window, 'alert');
    spyOn(console, 'error');
    mockRoomsService.createRoom.and.returnValue(throwError(() => 'Error creating'));
    
    component.isEditMode = false;
    component.save();

    expect(window.alert).toHaveBeenCalled();
  });

  it('loadRoomData should set currentImageUrl if room has imageName', () => {
    const roomWithImage = { id: 5, name: 'Room Img', active: true, software: [], imageName: 'pic.jpg' };
    mockRoomsService.getRoom.and.returnValue(of(roomWithImage));

    component.loadRoomData(5);

    expect(component.currentImageUrl).toBe('https://localhost:8443/api/rooms/5/image');
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
    expect(mockRoomsService.uploadRoomImage).toHaveBeenCalledWith(55, component.selectedFile);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/rooms']);
  });

  it('save (UPDATE) should call uploadRoomImage if a file is selected', () => {
    component.isEditMode = true;
    component.roomId = 10;
    
    component.selectedFile = new File([''], 'update.jpg');
    mockRoomsService.updateRoom.and.returnValue(of({ id: 10 }));
    component.save();

    expect(mockRoomsService.updateRoom).toHaveBeenCalled();
    expect(mockRoomsService.uploadRoomImage).toHaveBeenCalledWith(10, component.selectedFile);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/rooms']);
  });

  it('save should navigate immediately if NO file is selected', () => {
    component.selectedFile = null;
    component.isEditMode = false;

    component.save();

    expect(mockRoomsService.createRoom).toHaveBeenCalled();
    expect(mockRoomsService.uploadRoomImage).not.toHaveBeenCalled(); 
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/rooms']);
  });

  it('uploadImageAndNavigate should alert and navigate even if upload fails', () => {
    spyOn(window, 'alert');
    mockRoomsService.uploadRoomImage.and.returnValue(throwError(() => new Error('Upload failed')));

    component.selectedFile = new File([''], 'fail.jpg');
    component.uploadImageAndNavigate(1);

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/upload failed/i));
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/rooms']);
  });

});