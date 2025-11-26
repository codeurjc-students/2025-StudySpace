import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OccupancyStatsComponent } from './occupancy-stats.component';

describe('OccupancyStatsComponent', () => {
  let component: OccupancyStatsComponent;
  let fixture: ComponentFixture<OccupancyStatsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OccupancyStatsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OccupancyStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
