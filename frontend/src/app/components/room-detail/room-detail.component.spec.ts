import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RoomDetailComponent } from './room-detail.component';
import { RoomsService } from '../../services/rooms.service';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';

describe('RoomDetailComponent', () => {
  let component: RoomDetailComponent;
  let fixture: ComponentFixture<RoomDetailComponent>;
  let mockRoomsService: any;

  beforeEach(async () => {
    mockRoomsService = {
      // Mock of disable room
      getRoom: jasmine.createSpy('getRoom').and.returnValue(of({
        id: 1, 
        name: 'Lab Disabled', 
        capacity: 20, 
        place: 'B1', 
        active: false, 
        software: []
      }))
    };

    await TestBed.configureTestingModule({
      declarations: [ RoomDetailComponent ],
      imports: [ RouterTestingModule ],
      providers: [
        { provide: RoomsService, useValue: mockRoomsService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { params: { id: '1' } } } 
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoomDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load room details', () => {
    expect(component).toBeTruthy();
    expect(mockRoomsService.getRoom).toHaveBeenCalledWith('1');
    expect(component.room?.name).toBe('Lab Disabled');
  });
  it('should create and load disabled room details', () => {
    expect(component).toBeTruthy();
    expect(component.room?.name).toBe('Lab Disabled');
    expect(component.room?.active).toBeFalse();
  });

  it('should show disabled warning if room is inactive', () => {
    fixture.detectChanges();
    const alertElement = fixture.nativeElement.querySelector('.alert.alert-danger');
    expect(alertElement).toBeTruthy();
    expect(alertElement.textContent).toContain('Temporarily Unavailable');
  });
});