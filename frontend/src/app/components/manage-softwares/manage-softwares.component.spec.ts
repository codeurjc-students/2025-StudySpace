import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageSoftwaresComponent } from './manage-softwares.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { SoftwareService } from '../../services/software.service';
import { of, throwError } from 'rxjs';
import { PaginationComponent } from '../pagination/pagination.component';
import { FormsModule } from '@angular/forms';
import { DialogService } from '../../services/dialog.service';

describe('ManageSoftwaresComponent', () => {
  let component: ManageSoftwaresComponent;
  let fixture: ComponentFixture<ManageSoftwaresComponent>;
  let softwareServiceSpy: jasmine.SpyObj<SoftwareService>;
  let dialogServiceSpy: jasmine.SpyObj<any>;

  const mockPage = {
    content: [{ id: 1, name: 'Java', version: '17', description: 'JDK' }],
    totalPages: 2,
    number: 0,
    size: 10,
  };

  beforeEach(async () => {
    softwareServiceSpy = jasmine.createSpyObj('SoftwareService', [
      'getAllSoftwares',
      'deleteSoftware',
      'searchSoftwares',
    ]);

    dialogServiceSpy = jasmine.createSpyObj('DialogService', [
      'alert',
      'confirm',
    ]);
    dialogServiceSpy.alert.and.returnValue(Promise.resolve());
    dialogServiceSpy.confirm.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      declarations: [ManageSoftwaresComponent, PaginationComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, FormsModule],
      providers: [
        { provide: SoftwareService, useValue: softwareServiceSpy },
        { provide: DialogService, useValue: dialogServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ManageSoftwaresComponent);
    component = fixture.componentInstance;

    softwareServiceSpy.getAllSoftwares.and.returnValue(of(mockPage as any));

    fixture.detectChanges();
  });

  it('should create and load softwares', () => {
    expect(component).toBeTruthy();
    expect(softwareServiceSpy.getAllSoftwares).toHaveBeenCalledWith(0);
    expect(component.softwares.length).toBe(1);
  });

  it('should handle load error', () => {
    spyOn(console, 'error');
    softwareServiceSpy.getAllSoftwares.and.returnValue(
      throwError(() => 'Error'),
    );
    component.loadSoftwares(1);
    expect(console.error).toHaveBeenCalled();
  });

  it('should delete software if confirmed', async () => {
    dialogServiceSpy.confirm.and.returnValue(Promise.resolve(true));
    softwareServiceSpy.deleteSoftware.and.returnValue(of({}));

    component.deleteSoftware(10);
    await fixture.whenStable();

    expect(softwareServiceSpy.deleteSoftware).toHaveBeenCalledWith(10);
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Success',
      'Software deleted!',
    );
  });

  it('should NOT delete if user cancels', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteSoftware(1);
    expect(softwareServiceSpy.deleteSoftware).not.toHaveBeenCalled();
  });

  it('should handle error during deletion', async () => {
    dialogServiceSpy.confirm.and.returnValue(Promise.resolve(true));
    softwareServiceSpy.deleteSoftware.and.returnValue(throwError(() => 'Fail'));

    component.deleteSoftware(1);
    await fixture.whenStable();

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Error',
      'Error deleting software.',
    );
  });

  it('pagination logic', () => {
    component.pageData = { totalPages: 2 } as any;
    expect(component.getVisiblePages()).toEqual([0, 1]);
  });

  it('onSearch: should call clearSearch if all fields are empty', () => {
    spyOn(component, 'clearSearch');
    component.searchText = '';
    component.minVersion = null;

    component.onSearch();

    expect(component.clearSearch).toHaveBeenCalled();
  });

  it('onSearch: should activate search mode and load page 0', () => {
    spyOn(component, 'loadSoftwares');
    component.searchText = 'Java';

    component.onSearch();

    expect(component.isSearching).toBeTrue();
    expect(component.loadSoftwares).toHaveBeenCalledWith(0);
  });

  it('clearSearch: should clean filters and load normal page', () => {
    spyOn(component, 'loadSoftwares');
    component.searchText = 'Matlab';
    component.minVersion = 10;
    component.isSearching = true;

    component.clearSearch();

    expect(component.searchText).toBe('');
    expect(component.minVersion).toBeNull();
    expect(component.isSearching).toBeFalse();
    expect(component.loadSoftwares).toHaveBeenCalledWith(0);
  });

  it('loadSoftwares: should call searchSoftwares when isSearching is true', () => {
    component.isSearching = true;
    component.searchText = 'Java';
    softwareServiceSpy.searchSoftwares.and.returnValue(of(mockPage as any));

    component.loadSoftwares(0);

    expect(softwareServiceSpy.searchSoftwares).toHaveBeenCalledWith(
      'Java',
      undefined,
      0,
    );
    expect(component.softwares.length).toBeGreaterThan(0);
  });
});
