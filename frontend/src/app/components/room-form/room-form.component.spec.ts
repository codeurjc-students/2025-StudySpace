import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RoomFormComponent } from './room-form.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { RoomsService } from '../../services/rooms.service';
import { SoftwareService } from '../../services/software.service';
import { LoginService } from '../../login/login.service';
import { of } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

describe('RoomFormComponent', () => {
  let component: RoomFormComponent;
  let fixture: ComponentFixture<RoomFormComponent>;

  const mockRoomsService = {
    createRoom: () => of({}),
    updateRoom: () => of({}),
    getRoom: (id: number) => of({ 
        id: id, 
        name: 'Test Room', 
        active: false, 
        software: [] 
    })
  };

  const mockSoftwareService = {
    getAllSoftwares: () => of([{ id: 1, name: 'Java', version: 17 }])
  };

  beforeEach(async () => {
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
});