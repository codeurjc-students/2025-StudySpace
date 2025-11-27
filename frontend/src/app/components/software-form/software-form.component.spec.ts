import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SoftwareFormComponent } from './software-form.component';

describe('SoftwareFormComponent', () => {
  let component: SoftwareFormComponent;
  let fixture: ComponentFixture<SoftwareFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SoftwareFormComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SoftwareFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
