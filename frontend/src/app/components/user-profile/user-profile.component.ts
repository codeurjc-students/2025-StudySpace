import { Component, OnInit } from '@angular/core';
import { LoginService } from '../../login/login.service';
import { ReservationService } from '../../services/reservation.service';
import { UserDTO } from '../../dtos/user.dto';
import { Location } from '@angular/common';//for navigation back to the previous page

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {

  user: UserDTO | null = null;
  isEditing = false;//to hide the edit profile form when you are not editing

  editData = { name: '', email: '' };

  constructor(public readonly loginService: LoginService, private readonly reservationService: ReservationService, private readonly location: Location) { }

  ngOnInit(): void {
    // We obtain the current user from the login service
    
    this.loginService.reloadUser().subscribe({
        next: (freshUser) => {
            this.user = freshUser;
            
            this.editData.name = this.user.name;
            this.editData.email = this.user.email;
        },
        error: (err) => console.error("Error loading profile", err)
    });
  }

  goBack() {
    this.location.back(); //for navigation back to the previous page
  }

  
  saveProfile() {
    this.loginService.updateProfile(this.editData.name, this.editData.email).subscribe({
        next: (updatedUser) => {
            alert("Profile updated successfully. Please log in again if you changed your email address.");
            
            if (this.user) {
                this.user.name = updatedUser.name;
                
                
                this.loginService.currentUser = this.user;
            }
            this.isEditing = false;
        },
        error: (err) => alert("Error updating profile")
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
                
                if (this.user && this.user.reservations) {
                    this.user.reservations = this.user.reservations.filter(r => r.id !== id);
                }
            },
            error: (e) => alert("Cancellation failed.")
        });
    }
  }
}