import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { UserDTO } from '../../dtos/user.dto';

@Component({
  selector: 'app-manage-users',
  templateUrl: './manage-users.component.html',
  styleUrls: ['./manage-users.component.css']
})
export class ManageUsersComponent implements OnInit {

  users: UserDTO[] = [];

  constructor(private userService: UserService) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers() {
    this.userService.getUsers().subscribe({
        next: (data) => this.users = data,
        error: (e) => console.error(e)
    });
  }

  toggleAdmin(user: UserDTO) {
    const isNowAdmin = !user.roles.includes('ADMIN'); //if then was admin, now not and viceversa  
    this.userService.changeRole(user.id, isNowAdmin).subscribe(() => this.loadUsers());
  }

  toggleBlock(user: UserDTO) {
    this.userService.toggleBlock(user.id).subscribe(() => this.loadUsers());
  }

  deleteUser(user: UserDTO) {
    if(confirm(`Are you sure you want to delete ${user.name}? This action cannot be undone.`)) {
        this.userService.deleteUser(user.id).subscribe(() => this.loadUsers());
    }
  }
  
  //to see bookings
  viewReservations(user: UserDTO) {
      alert("Functionality to view reservations" + user.name + " pending visual implementation.");
      //navigate to reservations page with user id as parameter
  }
}