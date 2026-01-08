import { Component, OnInit } from '@angular/core';
import { LoginService } from '../../login/login.service';
import { ReservationService } from '../../services/reservation.service';
import { UserDTO } from '../../dtos/user.dto';
import { Location } from '@angular/common';
import { Page } from '../../dtos/page.model';
import { PaginationUtil } from '../../utils/pagination.util';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {

  user: UserDTO | null = null;
  reservations: any[] = [];
  pageData?: Page<any>;     
  currentPage: number = 0;
  isEditing = false;
  editData = { name: '', email: '' };
  isChangingPassword = false;
  passwordData = { oldPassword: '', newPassword: '' };

  //oldPasswordVisible: boolean = false;
  //newPasswordVisible: boolean = false;

  constructor(
    public readonly loginService: LoginService, 
    private readonly reservationService: ReservationService, 
    private readonly location: Location
  ) { }

  ngOnInit(): void {
    this.loginService.reloadUser().subscribe({
        next: (freshUser: UserDTO | null) => {
          if(freshUser){
            this.user = freshUser;
            this.editData.name = this.user.name;
            this.editData.email = this.user.email;
            this.loadReservations(0);
          }else{
            console.warn("No user session found in profile.");
            this.user = null;
            this.editData = { name: '', email: '' };
          }
        },
        error: (err: any) => console.error("Error loading profile", err)
    });
  }
  // AUXILIAR METHOD
  loadReservations(page: number) {
    this.reservationService.getMyReservations(page).subscribe({
      next: (data) => {
        this.pageData = data;
        this.reservations = data.content; 
        this.currentPage = data.number;
      },
      error: (err) => console.error('Error loading reservations', err)
    });
  }

  getVisiblePages(): number[] {
    return PaginationUtil.getVisiblePages(this.pageData, this.currentPage);
  }


  goBack() {
    this.location.back();
  }
  
  saveProfile() {
    this.loginService.updateProfile(this.editData.name, this.editData.email).subscribe({
        next: (updatedUser: UserDTO) => {
            alert("Profile updated successfully.");
            
            if (this.user) {
                this.user.name = updatedUser.name;
                this.loginService.currentUser = this.user;
            }
            this.isEditing = false;
        },
        error: (err: any) => alert("Error updating profile")
    });
  }

  

  toggleEdit() {
    this.isEditing = !this.isEditing;
    if(this.user) { 
        this.editData.name = this.user.name;
        this.editData.email = this.user.email;
    }
  }

  cancelReservation(id: number) {
    if(confirm("Are you sure you want to cancel this reservation?")) {
        this.reservationService.deleteReservation(id).subscribe({
            next: () => {
                alert("Reservation cancelled.");
                if (this.user?.reservations) {
                    this.user.reservations = this.user.reservations.filter(r => r.id !== id);
                }
            },
            error: (e: any) => alert("Cancellation failed.")
        });
    }
  }
  
  performCancel(id: number) {
    if (confirm("Are you sure you want to cancel this reservation?")) {
      this.reservationService.cancelReservation(id).subscribe({
        next: () => {
          alert("Reservation successfully cancelled.");
          this.loadReservations(this.currentPage); 
        },
        error: (err) => {
          console.error(err);
          alert("Cancellation error:");
        }
      });
    }
  }

  //active only if not canceled and endDate is in the future
  isReservationActive(res: any): boolean {
    if (!res || !res.endDate) return false;
    
    const now = new Date();
    const end = new Date(res.endDate);
    
    return !res.cancelled && end > now;
  }

  changePassword() {
    if (!this.passwordData.oldPassword || !this.passwordData.newPassword) {
      alert("Please fill in both password fields.");
      return;
    }
    const passwordPattern = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&.])(?=\S+$).{8,}$/;
    if (!passwordPattern.test(this.passwordData.newPassword)) {
        alert('Password must contain:\n- At least 8 characters\n- One uppercase letter\n- One lowercase letter\n- One number\n- One special character (@$!%*?&.)');
        return; 
    }
    this.loginService.changePassword(this.passwordData.oldPassword, this.passwordData.newPassword).subscribe({
      next: (response) => {
        alert("Password updated successfully!");
        this.toggleChangePassword(); //clean data
      },
      error: (err) => {
        console.error(err);
        //for 400 form backend
        const msg = err.error?.message || "Failed to update password. Check your current password.";
        alert(msg);
      }
    });
  }



  toggleChangePassword() { //clean data
    this.isChangingPassword = !this.isChangingPassword;
    this.passwordData = { oldPassword: '', newPassword: '' };

  }


  /*toggleOldPasswordVisibility() {
    this.oldPasswordVisible = !this.oldPasswordVisible;
  }

  toggleNewPasswordVisibility() {
    this.newPasswordVisible = !this.newPasswordVisible;
  }*/
  
}



