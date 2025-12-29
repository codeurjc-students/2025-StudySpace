import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageUsersComponent } from './manage-users.component';
import { UserService } from '../../services/user.service';
import { of } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { UserDTO } from '../../dtos/user.dto'; 
import { LoginService } from '../../login/login.service';

describe('ManageUsersComponent', () => {
  let component: ManageUsersComponent;
  let fixture: ComponentFixture<ManageUsersComponent>;
  let mockUserService: any;
  let mockLoginService: any;

  // Mock users
  const mockUser1: UserDTO = { 
    id: 1, 
    name: 'User1', 
    email: 'u1@test.com', 
    roles: ['USER'], 
    blocked: false, 
    reservations: [] 
  };
  
  const mockUserAdmin: UserDTO = { 
    id: 2, 
    name: 'Admin1', 
    email: 'a1@test.com', 
    roles: ['USER', 'ADMIN'], 
    blocked: true, 
    reservations: [] 
  };

  beforeEach(async () => {
    mockUserService = {
      getUsers: jasmine.createSpy('getUsers').and.returnValue(of({
        content: [mockUser1, mockUserAdmin],
        totalPages: 1,
        totalElements: 2,
        last: true,
        first: true,
        size: 10,
        number: 0,
        numberOfElements: 2,
        sort: []
      })),
      changeRole: jasmine.createSpy('changeRole').and.returnValue(of({})),
      toggleBlock: jasmine.createSpy('toggleBlock').and.returnValue(of({})),
      deleteUser: jasmine.createSpy('deleteUser').and.returnValue(of({}))
    };

    mockLoginService = {
      currentUser: mockUser1 
    };

    await TestBed.configureTestingModule({
      declarations: [ ManageUsersComponent ],
      imports: [ RouterTestingModule ],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load users', () => {
    expect(component).toBeTruthy();
    expect(component.users.length).toBe(1);
    expect(mockUserService.getUsers).toHaveBeenCalled();
  });

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

  it('should delete user if confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    
    component.deleteUser(mockUser1);
    
    expect(mockUserService.deleteUser).toHaveBeenCalledWith(1);
    expect(mockUserService.getUsers).toHaveBeenCalledTimes(2);
  });

  it('should NOT delete user if NOT confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    
    component.deleteUser(mockUser1);
    
    expect(mockUserService.deleteUser).not.toHaveBeenCalled();
  });

  it('should show alert on viewReservations', () => {
    spyOn(window, 'alert');
    component.viewReservations(mockUser1);
    expect(window.alert).toHaveBeenCalled();
  });

  it('should filter out the current logged-in user from the list', () => {// 2 users and only one on the list, not us
    mockLoginService.currentUser = mockUser1; 

    component.loadUsers(0);

    // Verificaciones
    expect(component.users.length).toBe(1); 
    expect(component.users).not.toContain(mockUser1); 
    expect(component.users).toContain(mockUserAdmin); 
  });
});