import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { RoomDetailComponent } from './room-detail.component';
import { RoomsService } from '../../services/rooms.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';

describe('RoomDetailComponent', () => {
  let component: RoomDetailComponent;
  let fixture: ComponentFixture<RoomDetailComponent>;
  let mockRoomsService: any;

  const defaultRoom = { id: 1, name: 'Lab 1', active: true, software: [] };
  const mockStats = {
    hourlyStatus: { "8": false, "9": true },
    occupiedPercentage: 50,
    freePercentage: 50
  };

  beforeEach(async () => {
    mockRoomsService = {

      getRoom: jasmine.createSpy('getRoom').and.returnValue(of(defaultRoom)),
      getRoomStats: jasmine.createSpy('getRoomStats').and.returnValue(of(mockStats))
    };

    await TestBed.configureTestingModule({
      declarations: [ RoomDetailComponent ],
      imports: [ RouterTestingModule, FormsModule ],
      providers: [
        { provide: RoomsService, useValue: mockRoomsService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { params: { id: '1' } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RoomDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create and load room details (default active)', () => {
    fixture.detectChanges(); 
    
    expect(component).toBeTruthy();
    expect(mockRoomsService.getRoom).toHaveBeenCalledWith(1);
    expect(component.room?.name).toBe('Lab 1');
    expect(component.room?.active).toBeTrue();
  });

  it('should create and load disabled room details', () => {
    // overwirte the mock before starting the component
    const disabledRoom = { id: 1, name: 'Lab Disabled', active: false, software: [] };
    mockRoomsService.getRoom.and.returnValue(of(disabledRoom));

    fixture.detectChanges(); // Inicia ngOnInit con el nuevo mock

    expect(component.room?.name).toBe('Lab Disabled');
    expect(component.room?.active).toBeFalse();
    
    // Verify
    const alertElement = fixture.nativeElement.querySelector('.alert.alert-danger');
    expect(alertElement).toBeTruthy();
  });

  it('should load stats after initialization (async)', fakeAsync(() => {
    fixture.detectChanges(); 
    
    //setTimeout for ngOnInit
    tick(150); 
    
    const today = new Date().toISOString().split('T')[0];
    expect(mockRoomsService.getRoomStats).toHaveBeenCalledWith(1, today);
  }));

  it('should reload stats when date changes', () => {
    fixture.detectChanges();
    
    component.roomId = 1;
    component.selectedDate = '2025-12-25';
    
    component.onDateChange();
    
    expect(mockRoomsService.getRoomStats).toHaveBeenCalledWith(1, '2025-12-25');
  });


  it('should call destroyCharts before creating new ones to avoid memory leaks', () => {
    spyOn(component, 'destroyCharts').and.callThrough();
    
    component.roomId = 1;
    component.loadStats();
    
    expect(component.destroyCharts).toHaveBeenCalled();
  });

  it('should transform backend stats correctly for the Chart data', () => {
    
    const mockComplexStats = {
      hourlyStatus: { "08:00": false, "09:00": true, "10:00": true },
      occupiedPercentage: 66,
      freePercentage: 34
    };

    component.hourlyCanvas = { nativeElement: document.createElement('canvas') } as any;
    component.occupancyCanvas = { nativeElement: document.createElement('canvas') } as any;

    spyOn(component, 'createCharts').and.callThrough();
    
    component.createCharts(mockComplexStats);

    expect(component.createCharts).toHaveBeenCalledWith(mockComplexStats);
    
    expect((component as any).hourlyChart).toBeDefined();
    expect((component as any).occupancyChart).toBeDefined();
    

    const chartInstance = (component as any).occupancyChart;
    const data = chartInstance.data.datasets[0].data;
    
    expect(data[0]).toBe(66); // Occupied
    expect(data[1]).toBe(34); // Free
  });

  it('should not crash if canvas elements are missing', () => {
    component.hourlyCanvas = undefined!;
    component.occupancyCanvas = undefined!;

    const safeExecution = () => component.createCharts(mockStats);
    
    expect(safeExecution).not.toThrow();
  });






  it('should display correct image URL in HTML when room has imageName', () => {
    const roomWithImage = { id: 10, name: 'Lab Image', active: true, software: [], imageName: 'lab_pic.jpg' };
    mockRoomsService.getRoom.and.returnValue(of(roomWithImage));
    
    component.ngOnInit();
    fixture.detectChanges();

    const imgElement: HTMLImageElement = fixture.nativeElement.querySelector('.col-md-6 img');
    
    expect(imgElement).toBeTruthy();
    expect(imgElement.src).toContain('/api/rooms/10/image');
  });

  it('should display default asset when room has no imageName', () => {
    const roomNoImage = { id: 11, name: 'Lab No Image', active: true, software: [], imageName: null };
    mockRoomsService.getRoom.and.returnValue(of(roomNoImage));
    
    component.ngOnInit();
    fixture.detectChanges();

    const imgElement: HTMLImageElement = fixture.nativeElement.querySelector('.col-md-6 img');
    expect(imgElement.src).toContain('assets/default_image.png');
  });
});