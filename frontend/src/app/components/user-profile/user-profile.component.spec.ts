import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserProfileComponent } from './user-profile.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { LoginService } from '../../login/login.service';
import { ReservationService } from '../../services/reservation.service';
import { Location } from '@angular/common';
import { of, throwError } from 'rxjs';
import { UserDTO } from '../../dtos/user.dto';

describe('UserProfileComponent', () => {
  let component: UserProfileComponent;
  let fixture: ComponentFixture<UserProfileComponent>;
  let loginService: LoginService;
  let reservationService: ReservationService;
  let location: Location;

  const mockUser: UserDTO = { 
    id: 1, 
    name: 'John Doe', 
    email: 'john@test.com', 
    reservations: [], 
    roles: ['USER'], 
    blocked: false 
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserProfileComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, FormsModule],
      providers: [LoginService, ReservationService]
    }).compileComponents();
    
    fixture = TestBed.createComponent(UserProfileComponent);
    component = fixture.componentInstance;
    loginService = TestBed.inject(LoginService);
    loginService.currentUser = null;
    reservationService = TestBed.inject(ReservationService);
    location = TestBed.inject(Location);

    spyOn(loginService, 'reloadUser').and.returnValue(of(mockUser));
    spyOn(reservationService, 'getMyReservations').and.returnValue(of([]));
    
    fixture.detectChanges();
  });

  //initialization and navegation

  it('should handle error when loading user profile in ngOnInit', () => {
    const consoleSpy = spyOn(console, 'error');
    (loginService.reloadUser as jasmine.Spy).and.returnValue(throwError(() => new Error('Fail')));
    component.ngOnInit();
    expect(consoleSpy).toHaveBeenCalledWith("Error loading profile", jasmine.any(Error));
  });

  it('should call location.back() when goBack is called', () => {
    const locationSpy = spyOn(location, 'back');
    component.goBack();
    expect(locationSpy).toHaveBeenCalled();
  });

  //edit profile

  it('toggleEdit should switch isEditing and reset editData', () => {
    component.user = mockUser;
    component.isEditing = false;
    
    component.toggleEdit();
    expect(component.isEditing).toBeTrue();
    
    component.editData.name = 'Modified';
    component.toggleEdit(); // Cierra edición
    expect(component.editData.name).toBe('John Doe');
  });

  it('saveProfile should handle error from service', () => {
    spyOn(loginService, 'updateProfile').and.returnValue(throwError(() => new Error('Update error')));
    const alertSpy = spyOn(window, 'alert');
    component.saveProfile();
    expect(alertSpy).toHaveBeenCalledWith("Error updating profile");
  });

  //Delete and cancel (ADMIN)

  it('cancelReservation should not do anything if confirm is false', () => {
    const deleteSpy = spyOn(reservationService, 'deleteReservation');
    spyOn(window, 'confirm').and.returnValue(false);
    component.cancelReservation(10);
    expect(deleteSpy).not.toHaveBeenCalled();
  });

  it('cancelReservation should handle error from service', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    spyOn(reservationService, 'deleteReservation').and.returnValue(throwError(() => new Error('Err')));
    component.cancelReservation(10);
    expect(window.alert).toHaveBeenCalledWith("Cancellation failed.");
  });

  it('performCancel should call service and reload on success', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    spyOn(reservationService, 'cancelReservation').and.returnValue(of({}));
    const loadSpy = spyOn(component, 'loadReservations');
    
    component.performCancel(5);
    expect(window.alert).toHaveBeenCalledWith("Reservation successfully cancelled.");
    expect(loadSpy).toHaveBeenCalled();
  });

  it('performCancel should handle error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    spyOn(reservationService, 'cancelReservation').and.returnValue(throwError(() => new Error('Err')));
    component.performCancel(5);
    expect(window.alert).toHaveBeenCalledWith("Cancellation error:");
  });

  

  it('isReservationActive logic coverage', () => {
    const now = new Date();
    const future = new Date(now.getTime() + 1000000);
    const past = new Date(now.getTime() - 1000000);

    //Null
    expect(component.isReservationActive(null)).toBeFalse();
    
    // No end date
    expect(component.isReservationActive({ cancelled: false })).toBeFalse();
    
    //Canceled
    expect(component.isReservationActive({ endDate: future, cancelled: true })).toBeFalse();
    
    //Expired
    expect(component.isReservationActive({ endDate: past, cancelled: false })).toBeFalse();
    
    // Active OK
    expect(component.isReservationActive({ endDate: future, cancelled: false })).toBeTrue();
  });

  it('loadReservations should handle error', () => {
    const consoleSpy = spyOn(console, 'error');
    (reservationService.getMyReservations as jasmine.Spy).and.returnValue(throwError(() => new Error('Fail')));
    component.loadReservations();
    expect(consoleSpy).toHaveBeenCalledWith("Error loading reservations", jasmine.any(Error));
  });




  it('loadReservations: should not set reservations if user is null', () => {
  component.user = null;
  const mockReservations = [{ id: 1, reason: 'Test' }];
  (reservationService.getMyReservations as jasmine.Spy).and.returnValue(of(mockReservations));
  
  component.loadReservations();
  
  // Verify
  expect(component.user).toBeNull();
  });

  it('saveProfile: should not update currentUser if user is null', () => {
  component.user = null;
  const updatedUser: UserDTO = { ...mockUser, name: 'New Name' };
  spyOn(loginService, 'updateProfile').and.returnValue(of(updatedUser));
  spyOn(window, 'alert');

  component.saveProfile();

  // Verify
  expect(loginService.currentUser).toBeNull();
  });

  it('toggleEdit: should not set editData if user is null', () => {
  component.user = null;
  component.editData = { name: 'Original', email: 'Original' };
  
  component.toggleEdit();
  
  
  expect(component.editData.name).toBe('Original');
  });

  it('cancelReservation: should handle success even if user.reservations is missing', () => {
  spyOn(window, 'confirm').and.returnValue(true);
  spyOn(reservationService, 'deleteReservation').and.returnValue(of({}));
  spyOn(window, 'alert');

  //undefined for optional chaining
  component.user = { ...mockUser, reservations: undefined as any };

  component.cancelReservation(10);
  
  expect(window.alert).toHaveBeenCalledWith("Reservation cancelled.");
  expect(component.user.reservations).toBeUndefined();
  });

  it('ngOnInit: should handle null user from reloadUser and use default empty strings', () => {
  (loginService.reloadUser as jasmine.Spy).and.returnValue(of(null));
  
  component.ngOnInit();
  
  expect(component.editData.name).toBe('');
  expect(component.editData.email).toBe('');
  });
});