import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RoomFormComponent } from './room-form.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { RoomsService } from '../../services/rooms.service';
import { SoftwareService } from '../../services/software.service';
import { LoginService } from '../../login/login.service';
import { of } from 'rxjs';

describe('RoomFormComponent', () => {
  let component: RoomFormComponent;
  let fixture: ComponentFixture<RoomFormComponent>;

  const mockRoomsService = {
    createRoom: () => of({}),
    updateRoom: () => of({}),
    getRoom: () => of({ name: 'Test Room', software: [] })
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
        LoginService
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
});