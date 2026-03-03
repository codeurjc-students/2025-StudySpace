import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageUsersComponent } from './manage-users.component';
import { UserService } from '../../services/user.service';
import { LoginService } from '../../login/login.service';
import { of, throwError } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { UserDTO } from '../../dtos/user.dto';
import { PaginationComponent } from '../pagination/pagination.component';
import { RoomsService } from '../../services/rooms.service';

describe('ManageUsersComponent', () => {
  let component: ManageUsersComponent;
  let fixture: ComponentFixture<ManageUsersComponent>;
  let mockUserService: any;
  let mockLoginService: any;
  let roomsServiceSpy: jasmine.SpyObj<RoomsService>;

  const mockUser1: UserDTO = {
    id: 1,
    name: 'User1',
    email: 'u1@test.com',
    roles: ['USER'],
    blocked: false,
    reservations: [],
  };
  const mockUserAdmin: UserDTO = {
    id: 2,
    name: 'Admin1',
    email: 'a1@test.com',
    roles: ['USER', 'ADMIN'],
    blocked: true,
    reservations: [],
  };

  const mockPageData = {
    content: [mockUser1, mockUserAdmin],
    totalPages: 5,
    number: 0,
    size: 10,
    first: true,
    last: false,
    totalElements: 50,
  };

  beforeEach(async () => {
    roomsServiceSpy = jasmine.createSpyObj('RoomsService', ['searchRooms']);
    roomsServiceSpy.searchRooms.and.returnValue(
      of({ content: [], number: 0, totalPages: 0 } as any),
    );
    mockUserService = {
      getUsers: jasmine.createSpy('getUsers').and.returnValue(of(mockPageData)),
      searchUsers: jasmine
        .createSpy('searchUsers')
        .and.returnValue(of(mockPageData)),
      changeRole: jasmine.createSpy('changeRole').and.returnValue(of({})),
      toggleBlock: jasmine.createSpy('toggleBlock').and.returnValue(of({})),
      deleteUser: jasmine.createSpy('deleteUser').and.returnValue(of({})),
    };

    mockLoginService = {
      currentUser: { id: 999 },
    };

    await TestBed.configureTestingModule({
      declarations: [ManageUsersComponent, PaginationComponent],
      imports: [RouterTestingModule, FormsModule],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: RoomsService, useValue: roomsServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ManageUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load users', () => {
    expect(component).toBeTruthy();
    expect(component.users.length).toBe(2);
    expect(mockUserService.getUsers).toHaveBeenCalledWith(0);
  });

  it('should handle error when loading users', () => {
    spyOn(console, 'error');
    mockUserService.getUsers.and.returnValue(
      throwError(() => new Error('Load error')),
    );

    component.isSearching = false;
    component.loadUsers(0);

    expect(console.error).toHaveBeenCalled();
  });

  // --- ACTIONS TESTS ---

  it('should toggle admin role', () => {
    component.toggleAdmin(mockUser1);
    expect(mockUserService.changeRole).toHaveBeenCalledWith(1, true);
    expect(mockUserService.getUsers).toHaveBeenCalledTimes(2);
  });

  it('should toggle block status', () => {
    component.toggleBlock(mockUser1);
    expect(mockUserService.toggleBlock).toHaveBeenCalledWith(1);
    expect(mockUserService.getUsers).toHaveBeenCalledTimes(2);
  });

  // --- DELETE TESTS ---

  it('should delete user if confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    component.deleteUser(mockUser1);
    expect(mockUserService.deleteUser).toHaveBeenCalledWith(1);
    expect(mockUserService.getUsers).toHaveBeenCalledTimes(2);
  });

  it('should NOT delete user if rejected', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteUser(mockUser1);
    expect(mockUserService.deleteUser).not.toHaveBeenCalled();
  });

  it('should handle error when deleting user', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    spyOn(console, 'error');

    mockUserService.deleteUser.and.returnValue(
      throwError(() => new Error('Delete failed')),
    );

    component.deleteUser(mockUser1);

    expect(console.error).toHaveBeenCalled();
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/Error/));
  });

  // --- PAGINATION ---
  it('should calculate pagination correctly', () => {
    component.pageData = { totalPages: 5 } as any;
    expect(component.getVisiblePages().length).toBe(5);

    component.pageData = { totalPages: 20 } as any;
    component.currentPage = 15;
    const pages = component.getVisiblePages();
    expect(pages.length).toBe(10);
    expect(pages).toContain(15);
  });

  it('should render user image URL in the table', () => {
    const userWithPic: UserDTO = {
      id: 5,
      name: 'User Pic',
      email: 'p@test.com',
      roles: [],
      blocked: false,
      reservations: [],
      imageName: 'avatar.png',
    };

    component.users = [userWithPic];
    fixture.detectChanges();

    const img: HTMLImageElement =
      fixture.nativeElement.querySelector('tbody tr td img');

    expect(img).toBeTruthy();
    expect(img.src).toContain('/api/users/5/image');
  });

  it('should render placeholder when user has no image', () => {
    const userNoPic: UserDTO = {
      id: 6,
      name: 'User NoPic',
      email: 'np@test.com',
      roles: [],
      blocked: false,
      reservations: [],
    };

    component.users = [userNoPic];
    fixture.detectChanges();

    const img: HTMLImageElement =
      fixture.nativeElement.querySelector('tbody tr td img');
    expect(img.src).toContain('assets/user_placeholder.png');
  });

  it('onSearch: should call clearSearch if all search fields are empty', () => {
    spyOn(component, 'clearSearch');
    component.searchText = '';
    component.filterBlocked = '';
    component.filterType = '';
    component.filterRoom = '';
    component.filterDate = '';

    component.onSearch();

    expect(component.clearSearch).toHaveBeenCalled();
  });

  it('onSearch: should set isSearching to true and load users if fields have data', () => {
    spyOn(component, 'loadUsers');
    component.searchText = 'Admin';

    component.onSearch();

    expect(component.isSearching).toBeTrue();
    expect(component.loadUsers).toHaveBeenCalledWith(0);
  });

  it('clearSearch: should reset fields and reload normal data', () => {
    spyOn(component, 'loadUsers');
    component.searchText = 'Test';
    component.isSearching = true;

    component.clearSearch();

    expect(component.searchText).toBe('');
    expect(component.filterBlocked).toBe('');
    expect(component.filterType).toBe('');
    expect(component.filterRoom).toBe('');
    expect(component.filterDate).toBe('');
    expect(component.isSearching).toBeFalse();
    expect(component.loadUsers).toHaveBeenCalledWith(0);
  });

  it('loadUsers: should use searchUsers when isSearching is true', () => {
    if (!mockUserService.searchUsers) {
      mockUserService.searchUsers = jasmine
        .createSpy()
        .and.returnValue(of(mockPageData));
    } else {
      mockUserService.searchUsers.and.returnValue(of(mockPageData));
    }

    component.isSearching = true;
    component.searchText = 'User1';
    component.loadUsers(0);

    expect(mockUserService.searchUsers).toHaveBeenCalled();
    expect(component.users.length).toBeGreaterThan(0);
  });

  it('onRoomSearchChange: should search rooms when text is provided', () => {
    component.filterRoom = 'Lab';
    roomsServiceSpy.searchRooms.and.returnValue(
      of({ content: [{ id: 1, name: 'Lab 1' }] } as any),
    );

    component.onRoomSearchChange();

    expect(roomsServiceSpy.searchRooms).toHaveBeenCalledWith('Lab');
    expect(component.availableRooms.length).toBe(1);
  });

  it('onRoomSearchChange: should clear available rooms when text is empty', () => {
    component.filterRoom = '';
    component.availableRooms = [{ id: 1, name: 'Lab 1' } as any];

    component.onRoomSearchChange();

    expect(component.availableRooms.length).toBe(0);
  });
});
