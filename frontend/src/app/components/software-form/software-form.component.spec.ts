import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SoftwareFormComponent } from './software-form.component';
import { SoftwareService } from '../../services/software.service';
import { Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

describe('SoftwareFormComponent', () => {
  let component: SoftwareFormComponent;
  let fixture: ComponentFixture<SoftwareFormComponent>;
  let mockSoftwareService: any;
  let mockRouter: any;
  let mockActivatedRoute: any;

  
  const configureModule = async (routeParams: any) => {
    mockSoftwareService = {
      getSoftware: jasmine.createSpy('getSoftware').and.returnValue(of({ id: 1, name: 'Java', version: '17', description: 'JDK' })),
      createSoftware: jasmine.createSpy('createSoftware').and.returnValue(of({})),
      updateSoftware: jasmine.createSpy('updateSoftware').and.returnValue(of({}))
    };
    mockRouter = { navigate: jasmine.createSpy('navigate') };
    mockActivatedRoute = { snapshot: { paramMap: { get: (key: string) => routeParams[key] } } };



    
    await TestBed.configureTestingModule({
      declarations: [ SoftwareFormComponent ],
      imports: [ FormsModule ],
      providers: [
        { provide: SoftwareService, useValue: mockSoftwareService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SoftwareFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  describe('Create Mode', () => {
    beforeEach(async () => {
      await configureModule({}); 
    });

    it('should initialize in Create Mode', () => {
      expect(component.isEditMode).toBeFalse();
    });

    it('should call createSoftware on save', () => {
      component.software = { name: 'Python', version: 3.9, description: 'Lang' };
      component.save();
      expect(mockSoftwareService.createSoftware).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/softwares']);
    });
  });

  describe('Edit Mode', () => {
    beforeEach(async () => {
      await configureModule({ id: '1' }); 
    });

    it('should initialize in Edit Mode and load data', () => {
      expect(component.isEditMode).toBeTrue();
      expect(mockSoftwareService.getSoftware).toHaveBeenCalledWith(1);
      expect(component.software.name).toBe('Java');
    });

    it('should call updateSoftware on save', () => {
      component.save(); 
      expect(mockSoftwareService.updateSoftware).toHaveBeenCalledWith(1, component.software);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/softwares']);
    });
  });
});