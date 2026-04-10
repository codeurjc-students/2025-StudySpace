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
import { DialogService } from '../../services/dialog.service';

describe('UserProfileComponent', () => {
  let component: UserProfileComponent;
  let fixture: ComponentFixture<UserProfileComponent>;
  let loginServiceSpy: jasmine.SpyObj<LoginService>;
  let reservationServiceSpy: jasmine.SpyObj<ReservationService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let dialogServiceSpy: jasmine.SpyObj<DialogService>;
  let location: Location;

  let mockUser: UserDTO;
  let mockReservationsPage: any;

  beforeEach(async () => {
    mockUser = {
      id: 1,
      name: 'John Doe',
      email: 'john@test.com',
      reservations: [],
      roles: ['USER'],
      blocked: false,
    };

    mockReservationsPage = {
      content: [
        {
          id: 1,
          reason: 'Meeting',
          endDate: new Date().toISOString(),
          cancelled: false,
        },
      ],
      totalPages: 5,
      number: 0,
      size: 10,
    };

    loginServiceSpy = jasmine.createSpyObj('LoginService', [
      'reloadUser',
      'updateProfile',
      'changePassword',
      'isAdmin',
      'isLogged',
    ]);

    reservationServiceSpy = jasmine.createSpyObj('ReservationService', [
      'getMyReservations',
      'cancelReservation',
      'deleteReservation',
    ]);

    userServiceSpy = jasmine.createSpyObj('UserService', ['uploadUserImage']);
    dialogServiceSpy = jasmine.createSpyObj('DialogService', [
      'alert',
      'prompt',
      'confirm',
    ]);

    dialogServiceSpy.alert.and.returnValue(Promise.resolve());
    dialogServiceSpy.confirm.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      declarations: [UserProfileComponent, PaginationComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, FormsModule],
      providers: [
        { provide: LoginService, useValue: loginServiceSpy },
        { provide: ReservationService, useValue: reservationServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
        { provide: DialogService, useValue: dialogServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserProfileComponent);
    component = fixture.componentInstance;
    location = TestBed.inject(Location);

    loginServiceSpy.reloadUser.and.returnValue(of(mockUser));
    loginServiceSpy.isAdmin.and.returnValue(false);
    loginServiceSpy.isLogged.and.returnValue(true);
    reservationServiceSpy.getMyReservations.and.returnValue(
      of(mockReservationsPage),
    );

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
    reservationServiceSpy.getMyReservations.and.returnValue(
      throwError(() => new Error('Load failed')),
    );
    component.loadReservations(1);
    expect(console.error).toHaveBeenCalled();
  });

  it('performCancel: should cancel reservation if confirmed', async () => {
    spyOn(window, 'confirm').and.returnValue(true);
    reservationServiceSpy.cancelReservation.and.returnValue(of({}));

    if (component.performCancel) {
      component.performCancel(10);
      await fixture.whenStable();
      expect(reservationServiceSpy.cancelReservation).toHaveBeenCalledWith(10);
      expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
        'Success',
        jasmine.stringMatching(/successfully cancelled/i),
      );
    }
  });

  it('cancelReservation: should delete/cancel reservation if confirmed', async () => {
    spyOn(window, 'confirm').and.returnValue(true); 
    reservationServiceSpy.deleteReservation.and.returnValue(of({}));
    reservationServiceSpy.cancelReservation.and.returnValue(of({}));

    component.cancelReservation(10);
    await fixture.whenStable();

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Success',
      jasmine.stringMatching(/Reservation cancelled/i),
    );
  });

  it('should NOT action if confirmation rejected', async () => {
    dialogServiceSpy.confirm.and.returnValue(Promise.resolve(false));
    spyOn(window, 'confirm').and.returnValue(false);

    reservationServiceSpy.cancelReservation.and.returnValue(of({}));
    reservationServiceSpy.deleteReservation.and.returnValue(of({}));

    if (component.performCancel) component.performCancel(10);
    component.cancelReservation(10);
    await fixture.whenStable();

    expect(reservationServiceSpy.cancelReservation).not.toHaveBeenCalled();
    expect(reservationServiceSpy.deleteReservation).not.toHaveBeenCalled();
  });

 it('should handle error on cancel/delete reservation', async () => {
    spyOn(window, 'confirm').and.returnValue(true); 
    reservationServiceSpy.deleteReservation.and.returnValue(
      throwError(() => new Error('Fail')),
    );

    component.cancelReservation(10);
    await fixture.whenStable();

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Error',
      jasmine.stringMatching(/Cancellation failed/i),
    );
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

  it('saveProfile should update profile successfully', async () => {
    const updatedUser = { ...mockUser, name: 'New Name' };
    loginServiceSpy.updateProfile.and.returnValue(of(updatedUser));

    component.editData.name = 'New Name';
    component.editData.email = 'john@test.com';
    component.saveProfile();
    await fixture.whenStable();

    expect(loginServiceSpy.updateProfile).toHaveBeenCalledWith(
      'New Name',
      'john@test.com',
    );
    expect(component.user?.name).toBe('New Name');
    expect(component.isEditing).toBeFalse();
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Success',
      jasmine.stringMatching(/updated successfully/i),
    );
  });

  it('saveProfile should handle error from backend', async () => {
    loginServiceSpy.updateProfile.and.returnValue(
      throwError(() => new Error('Update failed')),
    );

    component.saveProfile();
    await fixture.whenStable();

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Error',
      jasmine.stringMatching(/Error updating profile/i),
    );
  });

  //Password

  it('changePassword should validate empty fields', async () => {
    component.passwordData = { oldPassword: '', newPassword: '' };
    component.changePassword();
    await fixture.whenStable();
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Error',
      jasmine.stringMatching(/fill/i),
    );
  });

  it('changePassword should call service and success', async () => {
    loginServiceSpy.changePassword.and.returnValue(of({ status: 'SUCCESS' }));
    const toggleSpy = spyOn(component, 'toggleChangePassword');

    component.passwordData = {
      oldPassword: 'old',
      newPassword: 'StrongPass1!',
    };
    component.changePassword();
    await fixture.whenStable();

    expect(loginServiceSpy.changePassword).toHaveBeenCalledWith(
      'old',
      'StrongPass1!',
    );
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Success',
      jasmine.stringMatching(/successfully/i),
    );
    expect(toggleSpy).toHaveBeenCalled();
  });

  it('changePassword: should call service on valid input', async () => {
    const errorResponse = { error: { message: 'Wrong password' } };
    loginServiceSpy.changePassword.and.returnValue(
      throwError(() => errorResponse),
    );
    spyOn(console, 'error');

    component.passwordData = {
      oldPassword: 'wrong',
      newPassword: 'StrongPass1!',
    };
    component.changePassword();
    await fixture.whenStable();

    expect(console.error).toHaveBeenCalled();
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Error',
      'Wrong password',
    );
  });

  it('changePassword should handle backend error', async () => {
    const errorResponse = { error: { message: 'Wrong password' } };
    loginServiceSpy.changePassword.and.returnValue(
      throwError(() => errorResponse),
    );
    spyOn(console, 'error');

    component.passwordData = {
      oldPassword: 'wrong',
      newPassword: 'StrongPass1!',
    };
    component.changePassword();
    await fixture.whenStable();

    expect(console.error).toHaveBeenCalled();
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Error',
      'Wrong password',
    );
  });

  it('changePassword: should NOT call service if new password is weak', async () => {
    component.passwordData = { oldPassword: 'old', newPassword: 'weak' };
    component.changePassword();
    await fixture.whenStable();

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Error',
      jasmine.stringMatching(/Password must contain/),
    );
    expect(loginServiceSpy.changePassword).not.toHaveBeenCalled();
  });

  it('changePassword: should show error alert on failure', async () => {
    spyOn(console, 'error');

    component.passwordData = {
      oldPassword: 'old',
      newPassword: 'StrongPass1!',
    };

    loginServiceSpy.changePassword.and.returnValue(
      throwError(() => ({ error: { message: 'Wrong password' } })),
    );

    component.changePassword();
    await fixture.whenStable();

    expect(console.error).toHaveBeenCalled();
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Error',
      'Wrong password',
    );
  });

  //Pagination

  it('should determine if reservation is active', () => {
    const future = new Date();
    future.setDate(future.getDate() + 1);
    const past = new Date();
    past.setDate(past.getDate() - 1);

    expect(
      component.isReservationActive({
        endDate: future.toISOString(),
        cancelled: false,
      }),
    ).toBeTrue();
    expect(
      component.isReservationActive({
        endDate: future.toISOString(),
        cancelled: true,
      }),
    ).toBeFalse();
    expect(
      component.isReservationActive({
        endDate: past.toISOString(),
        cancelled: false,
      }),
    ).toBeFalse();
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
    const userWithImage: UserDTO = {
      ...mockUser,
      id: 99,
      imageName: 'photo.jpg',
    };
    const url = component.getUserImageUrl(userWithImage);
    expect(url).toBe('/api/users/99/image');
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

  it('saveProfile should call uploadUserImage if a file is selected', async () => {
    component.isEditing = true;
    component.editData.name = 'New Name';
    const fileToUpload = new File(['data'], 'avatar.png');
    component.selectedFile = fileToUpload;

    const updatedUser = { ...mockUser, name: 'New Name' };
    const userWithImage = { ...updatedUser, imageName: 'new_uuid.png' };

    loginServiceSpy.updateProfile.and.returnValue(of(updatedUser));
    userServiceSpy.uploadUserImage.and.returnValue(of(userWithImage));

    component.saveProfile();
    await fixture.whenStable();

    expect(loginServiceSpy.updateProfile).toHaveBeenCalled();
    expect(userServiceSpy.uploadUserImage).toHaveBeenCalledWith(
      1,
      fileToUpload,
    );
    expect(component.user?.imageName).toBe('new_uuid.png');
    expect(dialogServiceSpy.alert).toHaveBeenCalledWith(
      'Success',
      jasmine.stringMatching(/image updated/i),
    );
    expect(component.selectedFile).toBeNull();
  });

  it('saveProfile should NOT call uploadUserImage if NO file is selected', () => {
    component.selectedFile = null;
    component.editData.name = 'Just Name';

    loginServiceSpy.updateProfile.and.returnValue(
      of({ ...mockUser, name: 'Just Name' }),
    );

    component.saveProfile();

    expect(loginServiceSpy.updateProfile).toHaveBeenCalled();
    expect(userServiceSpy.uploadUserImage).not.toHaveBeenCalled();
  });
});
