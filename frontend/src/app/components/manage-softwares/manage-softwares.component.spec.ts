import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageSoftwaresComponent } from './manage-softwares.component';

describe('ManageSoftwaresComponent', () => {
  let component: ManageSoftwaresComponent;
  let fixture: ComponentFixture<ManageSoftwaresComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ManageSoftwaresComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ManageSoftwaresComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
