import { Component, OnInit } from '@angular/core';
import { LoginService } from '../../login/login.service';
import { UserDTO } from '../../dtos/user.dto';
import { Location } from '@angular/common';//for navigation back to the previous page

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {

  user: UserDTO | null = null;

  constructor(public loginService: LoginService,private location: Location) { }

  ngOnInit(): void {
    // We obtain the current user from the login service
    
    this.user = this.loginService.currentUser; 
  }

  goBack() {
    this.location.back(); //for navigation back to the previous page
  }

  // Not implemented yet
  editProfile() {
    console.log("Editar perfil...");
  }

  editReservation(id: number) {
    console.log("Editar reserva", id);
  }

  deleteReservation(id: number) {
    console.log("Borrar reserva", id);
  }
}