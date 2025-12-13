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
    getRoom: jasmine.createSpy('getRoom')
  };

  const mockSoftwareService = {
    getAllSoftwares: () => of([{ id: 1, name: 'Java', version: 17 }])
  };

  const mockRouter = { navigate: jasmine.createSpy('navigate') };

  beforeEach(async () => {
    mockRoomsService.createRoom.and.returnValue(of({ id: 1 }));
    mockRoomsService.updateRoom.and.returnValue(of({ id: 1 }));
    
    mockRoomsService.getRoom.and.returnValue(of({ 
        id: 1, 
        name: 'Test Room', 
        active: false, 
        software: [] 
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
          // Simulamos que siempre hay un ID '1' en la URL
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

});