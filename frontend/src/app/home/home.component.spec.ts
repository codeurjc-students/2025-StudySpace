import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeComponent } from './home.component';
import { RoomsService } from '../services/rooms.service';
import { LoginService } from '../login/login.service';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let mockRoomsService: any;
  let mockLoginService: any;

  const mockRoomsPage = {
    content: [
      { id: 1, name: 'Aula Magna', capacity: 100, camp: 'MOSTOLES', place: 'Aulario I', software: [] },
      { id: 2, name: 'Lab 1', capacity: 20, camp: 'ALCORCON', place: 'Lab II', software: [] }
    ],
    totalPages: 3, 
    number: 0, 
    size: 10,
    first: true,
    last: false,
    totalElements: 25
  };

  beforeEach(async () => {
    mockRoomsService = {
      getRooms: jasmine.createSpy('getRooms').and.returnValue(of(mockRoomsPage))
    };
    mockLoginService = {
      isLogged: () => true,
      isAdmin: () => false
    };

    await TestBed.configureTestingModule({
      declarations: [ HomeComponent ],
      imports: [ RouterTestingModule ],
      providers: [
        { provide: RoomsService, useValue: mockRoomsService },
        { provide: LoginService, useValue: mockLoginService }
      ]
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
    expect(compiled.querySelector('h3')?.textContent).toContain('Available Rooms');
  });

  it('should handle error when loading rooms', () => {
    spyOn(console, 'error');
    mockRoomsService.getRooms.and.returnValue(throwError(() => new Error('Load error')));
    
    component.loadPage(1);
    
    expect(console.error).toHaveBeenCalled();
  });

  it('should calculate visible pages correctly', () => {
    const pages = component.getVisiblePages();
    expect(pages.length).toBe(3);
    expect(pages).toEqual([0, 1, 2]);
    
    component.pageData = undefined;
    expect(component.getVisiblePages()).toEqual([]);
  });

  it('should handle sliding window pagination', () => {
    component.pageData = { totalPages: 50 } as any;
    
    //begining
    component.currentPage = 25;
    let pages = component.getVisiblePages();
    expect(pages.length).toBe(10);
    expect(pages).toContain(25);

    //middle
    component.currentPage = 0;
    pages = component.getVisiblePages();
    expect(pages[0]).toBe(0);


    //end
    component.currentPage = 49;
    pages = component.getVisiblePages();
    expect(pages[pages.length - 1]).toBe(49);
  });
});