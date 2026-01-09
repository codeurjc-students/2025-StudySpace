import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageUsersComponent } from './manage-users.component';
import { UserService } from '../../services/user.service';
import { LoginService } from '../../login/login.service';
import { of, throwError } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { UserDTO } from '../../dtos/user.dto';
import { PaginationComponent } from '../pagination/pagination.component';

describe('ManageUsersComponent', () => {
  let component: ManageUsersComponent;
  let fixture: ComponentFixture<ManageUsersComponent>;
  let mockUserService: any;
  let mockLoginService: any;

  const mockUser1: UserDTO = { id: 1, name: 'User1', email: 'u1@test.com', roles: ['USER'], blocked: false, reservations: [] };
  const mockUserAdmin: UserDTO = { id: 2, name: 'Admin1', email: 'a1@test.com', roles: ['USER', 'ADMIN'], blocked: true, reservations: [] };

  const mockPageData = {
    content: [mockUser1, mockUserAdmin],
    totalPages: 5,
    number: 0,
    size: 10,
    first: true,
    last: false,
    totalElements: 50
  };

  beforeEach(async () => {
    mockUserService = {
      getUsers: jasmine.createSpy('getUsers').and.returnValue(of(mockPageData)),
      changeRole: jasmine.createSpy('changeRole').and.returnValue(of({})),
      toggleBlock: jasmine.createSpy('toggleBlock').and.returnValue(of({})),
      deleteUser: jasmine.createSpy('deleteUser').and.returnValue(of({}))
    };

    mockLoginService = {
      currentUser: { id: 999 } 
    };

    await TestBed.configureTestingModule({
      declarations: [ ManageUsersComponent,PaginationComponent ],
      imports: [ RouterTestingModule, FormsModule ],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService }
      ]
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
    mockUserService.getUsers.and.returnValue(throwError(() => new Error('Load error')));
    component.loadUsers(1);
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

    mockUserService.deleteUser.and.returnValue(throwError(() => new Error('Delete failed')));

    component.deleteUser(mockUser1);

    expect(console.error).toHaveBeenCalled(); // Verificamos que se imprimió el error
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/Error/)); // Verificamos la alerta
  });

  it('should show alert on viewReservations', () => {
    spyOn(window, 'alert');
    component.viewReservations(mockUser1);
    expect(window.alert).toHaveBeenCalled();
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
    const userWithPic: UserDTO = { id: 5, name: 'User Pic', email: 'p@test.com', roles: [], blocked: false, reservations: [], imageName: 'avatar.png' };
    
    component.users = [userWithPic];
    fixture.detectChanges();

    const img: HTMLImageElement = fixture.nativeElement.querySelector('tbody tr td img');
    
    expect(img).toBeTruthy();
    expect(img.src).toContain('/api/users/5/image');
  });

  it('should render placeholder when user has no image', () => {
    const userNoPic: UserDTO = { id: 6, name: 'User NoPic', email: 'np@test.com', roles: [], blocked: false, reservations: [] };
    
    component.users = [userNoPic];
    fixture.detectChanges();

    const img: HTMLImageElement = fixture.nativeElement.querySelector('tbody tr td img');
    expect(img.src).toContain('assets/user_placeholder.png');
  });

});