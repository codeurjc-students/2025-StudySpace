import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeComponent } from './home.component';
import { RoomsService } from '../services/rooms.service';
import { LoginService } from '../login/login.service';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { PaginationComponent } from '../components/pagination/pagination.component';
import { FormsModule } from '@angular/forms';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let mockRoomsService: any;
  let mockLoginService: any;

  const mockRoomsPage = {
    content: [
      {
        id: 1,
        name: 'Aula Magna',
        capacity: 100,
        camp: 'MOSTOLES',
        place: 'Aulario I',
        software: [],
      },
      {
        id: 2,
        name: 'Lab 1',
        capacity: 20,
        camp: 'ALCORCON',
        place: 'Lab II',
        software: [],
      },
    ],
    totalPages: 3,
    number: 0,
    size: 10,
    first: true,
    last: false,
    totalElements: 25,
  };

  beforeEach(async () => {
    mockRoomsService = {
      getRooms: jasmine
        .createSpy('getRooms')
        .and.returnValue(of(mockRoomsPage)),
      searchRooms: jasmine
        .createSpy('searchRooms')
        .and.returnValue(of(mockRoomsPage)),
    };
    mockLoginService = {
      isLogged: () => true,
      isAdmin: () => false,
    };

    await TestBed.configureTestingModule({
      declarations: [HomeComponent, PaginationComponent],
      imports: [RouterTestingModule, FormsModule],
      providers: [
        { provide: RoomsService, useValue: mockRoomsService },
        { provide: LoginService, useValue: mockLoginService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load initial rooms', () => {
    expect(component).toBeTruthy();
    expect(component.rooms.length).toBe(2);
    expect(mockRoomsService.getRooms).toHaveBeenCalledWith(0);
  });

  it('should display "Available Rooms" title', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h3')?.textContent).toContain(
      'Available Rooms',
    );
  });

  it('should handle error when loading rooms', () => {
    spyOn(console, 'error');
    mockRoomsService.getRooms.and.returnValue(
      throwError(() => new Error('Load error')),
    );

    component.loadPage(1);

    expect(console.error).toHaveBeenCalled();
  });

  it('onSearch: should call clearSearch if all search fields are empty', () => {
    spyOn(component, 'clearSearch');
    component.searchText = '';
    component.selectedCampus = '';
    component.minCapacity = null;

    component.onSearch();

    expect(component.clearSearch).toHaveBeenCalled();
  });

  it('onSearch: should set isSearching to true and call loadPage if fields have data', () => {
    spyOn(component, 'loadPage');
    component.searchText = 'Aula';

    component.onSearch();

    expect(component.isSearching).toBeTrue();
    expect(component.loadPage).toHaveBeenCalledWith(0);
  });

  it('clearSearch: should reset search fields and reload page', () => {
    spyOn(component, 'loadPage');
    component.searchText = 'Lab';
    component.selectedCampus = 'ALCORCON';
    component.minCapacity = 30;
    component.isSearching = true;

    component.clearSearch();

    expect(component.searchText).toBe('');
    expect(component.selectedCampus).toBe('');
    expect(component.minCapacity).toBeNull();
    expect(component.isSearching).toBeFalse();
    expect(component.loadPage).toHaveBeenCalledWith(0);
  });

  it('loadPage: should call searchRooms if isSearching is true', () => {
    component.isSearching = true;
    component.searchText = 'Lab';

    component.loadPage(0);

    expect(mockRoomsService.searchRooms).toHaveBeenCalledWith(
      'Lab',
      undefined,
      undefined,
      true,
      0,
    );
    expect(component.rooms.length).toBeGreaterThan(0);
  });
});
