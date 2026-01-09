import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserProfileComponent } from './user-profile.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { LoginService } from '../../login/login.service';
import { ReservationService } from '../../services/reservation.service';
import { UserService } from '../../services/user.service';
import { Location } from '@angular/common';
import { of, throwError } from 'rxjs';
import { UserDTO } from '../../dtos/user.dto';
import { PaginationComponent } from '../pagination/pagination.component';

describe('UserProfileComponent', () => {
  let component: UserProfileComponent;
  let fixture: ComponentFixture<UserProfileComponent>;
  let loginServiceSpy: jasmine.SpyObj<LoginService>;
  let reservationServiceSpy: jasmine.SpyObj<ReservationService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let location: Location;

  let mockUser: UserDTO;
  let mockReservationsPage: any;

  beforeEach(async () => {
    mockUser = { 
      id: 1, name: 'John Doe', email: 'john@test.com', reservations: [], roles: ['USER'], blocked: false 
    };
    
    mockReservationsPage = {
      content: [{ id: 1, reason: 'Meeting', endDate: new Date().toISOString(), cancelled: false }],
      totalPages: 5,
      number: 0,
      size: 10
    };

    loginServiceSpy = jasmine.createSpyObj('LoginService', 
      ['reloadUser', 'updateProfile', 'changePassword', 'isAdmin', 'isLogged']
    );
    
    reservationServiceSpy = jasmine.createSpyObj('ReservationService', 
      ['getMyReservations', 'cancelReservation', 'deleteReservation']
    );

    userServiceSpy = jasmine.createSpyObj('UserService', ['uploadUserImage']);

    await TestBed.configureTestingModule({
      declarations: [UserProfileComponent, PaginationComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, FormsModule],
      providers: [
        { provide: LoginService, useValue: loginServiceSpy },
        { provide: ReservationService, useValue: reservationServiceSpy },
        { provide: UserService, useValue: userServiceSpy }
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(UserProfileComponent);
    component = fixture.componentInstance;
    location = TestBed.inject(Location);

    loginServiceSpy.reloadUser.and.returnValue(of(mockUser));
    loginServiceSpy.isAdmin.and.returnValue(false);
    loginServiceSpy.isLogged.and.returnValue(true);
    reservationServiceSpy.getMyReservations.and.returnValue(of(mockReservationsPage));

    fixture.detectChanges(); 
  });

  //initialization

  it('should create and load user data', () => {
    expect(component).toBeTruthy();
    expect(component.user).toEqual(mockUser);
    expect(component.editData.name).toBe('John Doe');
    expect(reservationServiceSpy.getMyReservations).toHaveBeenCalledWith(0);
  });

  it('should handle null user on reload', () => {
    loginServiceSpy.reloadUser.and.returnValue(of(null));
    component.ngOnInit();
    expect(component.user).toBeNull();
  });

  //Bookings

  it('should load reservations successfully', () => {
    component.loadReservations(2);
    expect(reservationServiceSpy.getMyReservations).toHaveBeenCalledWith(2);
    expect(component.currentPage).toBe(0);
  });

  it('should handle error when loading reservations', () => {
    spyOn(console, 'error'); 
    reservationServiceSpy.getMyReservations.and.returnValue(throwError(() => new Error('Load failed')));
    component.loadReservations(1);
    expect(console.error).toHaveBeenCalled();
  });

  it('performCancel: should cancel reservation if confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    reservationServiceSpy.cancelReservation.and.returnValue(of({}));

    if (component.performCancel) {
        component.performCancel(10);
        expect(reservationServiceSpy.cancelReservation).toHaveBeenCalledWith(10);
        expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/success|cancelled/i));
    }
  });

  it('cancelReservation: should delete/cancel reservation if confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    
    reservationServiceSpy.deleteReservation.and.returnValue(of({}));
    reservationServiceSpy.cancelReservation.and.returnValue(of({}));

    component.cancelReservation(10); 

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/cancelled|deleted|success/i));
  });

  it('should NOT action if confirmation rejected', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    
    if(component.performCancel) component.performCancel(10);
    component.cancelReservation(10);
    
    expect(reservationServiceSpy.cancelReservation).not.toHaveBeenCalled();
    expect(reservationServiceSpy.deleteReservation).not.toHaveBeenCalled();
  });

  it('should handle error on cancel/delete reservation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    
    reservationServiceSpy.deleteReservation.and.returnValue(throwError(() => new Error('Fail')));
    
    component.cancelReservation(10);
    
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/Error|Failed/i));
  });

  // edit profile

  it('should toggle edit mode correctly', () => {
    component.toggleEdit();
    expect(component.isEditing).toBeTrue();
    
    component.editData.name = 'Changed Name'; 
    component.toggleEdit(); 
    
    expect(component.isEditing).toBeFalse();
    expect(component.editData.name).toBe('John Doe'); 
  });

  it('saveProfile should update profile successfully', () => {
    const updatedUser = { ...mockUser, name: 'New Name' };
    loginServiceSpy.updateProfile.and.returnValue(of(updatedUser));
    spyOn(window, 'alert');

    component.editData.name = 'New Name';
    component.editData.email = 'john@test.com'; 
    component.saveProfile(); 

    expect(loginServiceSpy.updateProfile).toHaveBeenCalledWith('New Name', 'john@test.com');
    expect(component.user?.name).toBe('New Name');
    expect(component.isEditing).toBeFalse();
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/success|updated/i));
  });

  it('saveProfile should handle error from backend', () => {
    loginServiceSpy.updateProfile.and.returnValue(throwError(() => new Error('Update failed')));
    spyOn(window, 'alert');
    
    component.saveProfile();
    
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/Error|Failed/i));
  });

  //Password

  it('changePassword should validate empty fields', () => {
    spyOn(window, 'alert');
    component.passwordData = { oldPassword: '', newPassword: '' };
    component.changePassword();
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/fill/i));
  });

  it('changePassword should call service and success', () => {
    loginServiceSpy.changePassword.and.returnValue(of({ status: 'SUCCESS' }));
    spyOn(window, 'alert');
    const toggleSpy = spyOn(component, 'toggleChangePassword');

    component.passwordData = { oldPassword: 'old', newPassword: 'StrongPass1!' };
    component.changePassword();

    expect(loginServiceSpy.changePassword).toHaveBeenCalledWith('old', 'StrongPass1!');
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/success/i));
    expect(toggleSpy).toHaveBeenCalled(); 
  });

  it('changePassword: should call service on valid input', () => {
    spyOn(window, 'alert');
    component.passwordData = { oldPassword: 'old', newPassword: 'StrongPass1!' };
    
    loginServiceSpy.changePassword.and.returnValue(of({}));

    component.changePassword();

    expect(loginServiceSpy.changePassword).toHaveBeenCalledWith('old', 'StrongPass1!');
    expect(window.alert).toHaveBeenCalledWith('Password updated successfully!');
  });

  it('changePassword should handle backend error', () => {
    const errorResponse = { error: { message: 'Wrong password' } };
    loginServiceSpy.changePassword.and.returnValue(throwError(() => errorResponse));
    spyOn(window, 'alert');
    spyOn(console, 'error');

    component.passwordData = { oldPassword: 'wrong', newPassword: 'StrongPass1!' };
    component.changePassword();

    expect(console.error).toHaveBeenCalled();
    expect(window.alert).toHaveBeenCalledWith('Wrong password');
  });

  it('changePassword: should NOT call service if new password is weak', () => {
    spyOn(window, 'alert');
    
    component.passwordData = { oldPassword: 'old', newPassword: 'weak' };

    component.changePassword();

    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/Password must contain/));
    expect(loginServiceSpy.changePassword).not.toHaveBeenCalled();
  });

  it('changePassword: should show error alert on failure', () => {
    spyOn(window, 'alert');
    spyOn(console, 'error');
    
    component.passwordData = { oldPassword: 'old', newPassword: 'StrongPass1!' }; // Strong pass necesario para pasar el filtro
    
    loginServiceSpy.changePassword.and.returnValue(throwError({ error: { message: 'Wrong password' } }));

    component.changePassword();

    expect(console.error).toHaveBeenCalled();
    expect(window.alert).toHaveBeenCalledWith('Wrong password');
  });

  //Pagination

  it('should determine if reservation is active', () => {
    const future = new Date(); future.setDate(future.getDate() + 1);
    const past = new Date(); past.setDate(past.getDate() - 1);

    expect(component.isReservationActive({ endDate: future.toISOString(), cancelled: false })).toBeTrue();
    expect(component.isReservationActive({ endDate: future.toISOString(), cancelled: true })).toBeFalse();
    expect(component.isReservationActive({ endDate: past.toISOString(), cancelled: false })).toBeFalse();
    expect(component.isReservationActive(null)).toBeFalse();
  });

  
  it('pagination: should calculate visible pages correctly', () => {
    component.pageData = { totalPages: 5 } as any;
    expect(component.getVisiblePages().length).toBe(5);

    component.pageData = { totalPages: 50 } as any;
    component.currentPage = 25;
    const pages = component.getVisiblePages();
    
    expect(pages.length).toBeGreaterThanOrEqual(5); 
    expect(pages).toContain(25);
  });

  it('pagination: should return empty if no data', () => {
    component.pageData = undefined;
    expect(component.getVisiblePages()).toEqual([]);
  });

  it('goBack should call location.back', () => {
    spyOn(location, 'back');
    component.goBack();
    expect(location.back).toHaveBeenCalled();
  });





  it('getUserImageUrl should return correct API URL if image exists', () => {
    const userWithImage: UserDTO = { ...mockUser, id: 99, imageName: 'photo.jpg' };
    const url = component.getUserImageUrl(userWithImage);
    expect(url).toBe('https://localhost:8443/api/users/99/image');
  });

  it('getUserImageUrl should return placeholder if no image', () => {
    const userNoImage: UserDTO = { ...mockUser, imageName: undefined };
    const url = component.getUserImageUrl(userNoImage);
    expect(url).toBe('assets/user_placeholder.png');
  });

  it('onFileSelected should update selectedFile property', () => {
    const mockFile = new File([''], 'test.jpg', { type: 'image/jpeg' });
    const event = { target: { files: [mockFile] } };
    
    component.onFileSelected(event);
    
    expect(component.selectedFile).toEqual(mockFile);
  });

  it('saveProfile should call uploadUserImage if a file is selected', () => {
    //Setup
    component.isEditing = true;
    component.editData.name = 'New Name';
    const fileToUpload = new File(['data'], 'avatar.png');
    component.selectedFile = fileToUpload;
    
    const updatedUser = { ...mockUser, name: 'New Name' };
    const userWithImage = { ...updatedUser, imageName: 'new_uuid.png' };
    
    loginServiceSpy.updateProfile.and.returnValue(of(updatedUser));
    userServiceSpy.uploadUserImage.and.returnValue(of(userWithImage));
    spyOn(window, 'alert');

    component.saveProfile();

    expect(loginServiceSpy.updateProfile).toHaveBeenCalled();
    expect(userServiceSpy.uploadUserImage).toHaveBeenCalledWith(1, fileToUpload);
    
    expect(component.user?.imageName).toBe('new_uuid.png');
    expect(window.alert).toHaveBeenCalledWith(jasmine.stringMatching(/image updated/i));
    expect(component.selectedFile).toBeNull(); 
  });

  it('saveProfile should NOT call uploadUserImage if NO file is selected', () => {
    component.selectedFile = null;
    component.editData.name = 'Just Name';
    
    loginServiceSpy.updateProfile.and.returnValue(of({ ...mockUser, name: 'Just Name' }));
    
    component.saveProfile();

    expect(loginServiceSpy.updateProfile).toHaveBeenCalled();
    expect(userServiceSpy.uploadUserImage).not.toHaveBeenCalled();
  });


});