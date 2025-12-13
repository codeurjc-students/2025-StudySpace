import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { OccupancyStatsComponent } from './occupancy-stats.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('OccupancyStatsComponent', () => {
  let component: OccupancyStatsComponent;
  let fixture: ComponentFixture<OccupancyStatsComponent>;
  let httpMock: HttpTestingController;

  const mockStatsData = {
    totalRooms: 10,
    occupiedPercentage: 50,
    freePercentage: 50,
    roomsWithSoftwarePercentage: 80,
    roomsWithoutSoftwarePercentage: 20,
    hourlyOccupancy: { 10: 5 }
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OccupancyStatsComponent],
      imports: [
        HttpClientTestingModule,
        FormsModule
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OccupancyStatsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });





  it('should create', () => {
    expect(component).toBeTruthy();
  });

  //todays date initialized 
  it('should initialize selectedDate to today', fakeAsync(() => {
    fixture.detectChanges(); 
    tick(100); 
    

    const today = new Date();
    const year = today.getFullYear();
    const month = ('0' + (today.getMonth() + 1)).slice(-2);
    const day = ('0' + today.getDate()).slice(-2);
    const expectedDate = `${year}-${month}-${day}`;
    
    expect(component.selectedDate).toBe(expectedDate);

    const req = httpMock.expectOne(req => req.url.includes('/api/stats'));
    req.flush(mockStatsData);
  }));

  
  it('should call create charts functions after data load', fakeAsync(() => {
    spyOn(component, 'createOccupancyChart');
    spyOn(component, 'createHourlyChart');

    fixture.detectChanges(); 
    tick(100); 
    
    const req = httpMock.expectOne(req => req.url.includes('/api/stats'));
    req.flush(mockStatsData);
    //Verify
    expect(component.createOccupancyChart).toHaveBeenCalled();
    expect(component.createHourlyChart).toHaveBeenCalled();
  }));


  it('should reload data when onDateChange is called', fakeAsync(() => {
    fixture.detectChanges();
    tick(100);

    httpMock.expectOne(req => req.url.includes('/api/stats')).flush(mockStatsData);

    //manual date chage
    component.onDateChange();

    // Verify
    const req = httpMock.expectOne(req => req.url.includes('/api/stats'));
    expect(req.request.method).toBe('GET');
    req.flush(mockStatsData);
  }));

  
  it('should handle HTTP error gracefully', fakeAsync(() => {
    spyOn(console, 'error');

    fixture.detectChanges();
    tick(100);

    const req = httpMock.expectOne(req => req.url.includes('/api/stats'));
    
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });//500 error

    expect(console.error).toHaveBeenCalled();
  }));
});