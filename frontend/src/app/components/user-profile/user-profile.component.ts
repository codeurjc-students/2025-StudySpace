import { Component, OnInit } from '@angular/core';
import { LoginService } from '../../login/login.service';
import { ReservationService } from '../../services/reservation.service';
import { UserDTO } from '../../dtos/user.dto';
import { Location } from '@angular/common';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {

  user: UserDTO | null = null;
  isEditing = false;
  editData = { name: '', email: '' };

  constructor(
    public readonly loginService: LoginService, 
    private readonly reservationService: ReservationService, 
    private readonly location: Location
  ) { }

  ngOnInit(): void {
    // Usamos reloadUser que ahora sí existe en LoginService
    this.loginService.reloadUser().subscribe({
        next: (freshUser: UserDTO) => {
            this.user = freshUser;
            // Usamos optional chaining (?.) para evitar errores si viene null
            this.editData.name = this.user?.name || '';
            this.editData.email = this.user?.email || '';
            this.loadReservations();
        },
        error: (err: any) => console.error("Error loading profile", err)
    });
  }
  // AUXILIAR METHOD
  loadReservations() {
      this.reservationService.getMyReservations().subscribe({
          next: (reservations) => {
              if (this.user) {
                  this.user.reservations = reservations;
                  console.log("Load reservations:", reservations);
              }
          },
          error: (err) => console.error("Error loading reservations", err)
      });
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
                // Actualizamos también el currentUser del servicio para que la UI global se entere
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
}