import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { LoginService } from '../../login/login.service';
import { UserDTO } from '../../dtos/user.dto';
import { Page } from '../../dtos/page.model';

@Component({
  selector: 'app-manage-users',
  templateUrl: './manage-users.component.html',
  styleUrls: ['./manage-users.component.css']
})
export class ManageUsersComponent implements OnInit {

  users: UserDTO[] = [];
  pageData?: Page<UserDTO>; 
  currentPage: number = 0;  

  constructor(
    private readonly userService: UserService,
    private readonly loginService: LoginService
  ) { }

  ngOnInit(): void {
    this.loadUsers(0);
  }

  loadUsers(page: number) {
    this.userService.getUsers(page).subscribe({
        next: (data) => {
            this.pageData = data;
            //if you are on the list, will show 9 elements instead of 10
            const currentUserId = this.loginService.currentUser?.id;
            this.users = data.content.filter(user => user.id !== currentUserId);
            
            this.currentPage = data.number;
        },
        error: (e) => console.error(e)
    });
  }

  getPagesArray(): number[] {//Check if necessaryyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy
    return Array.from({ length: this.pageData?.totalPages || 0 }, (_, i) => i);
  }

  toggleAdmin(user: UserDTO) {
    const isNowAdmin = !user.roles.includes('ADMIN'); //if then was admin, now not and viceversa  
    this.userService.changeRole(user.id, isNowAdmin).subscribe(() => this.loadUsers(this.currentPage));
  }
  

  toggleBlock(user: UserDTO) {
    this.userService.toggleBlock(user.id).subscribe(() => this.loadUsers(this.currentPage));
  }

  deleteUser(user: UserDTO) {
    if(confirm(`Are you sure you want to delete ${user.name}? This action cannot be undone.`)) {
        this.userService.deleteUser(user.id).subscribe(() => this.loadUsers(this.currentPage));
    }
  }
  
  //to see bookings
  viewReservations(user: UserDTO) {
      alert("Functionality to view reservations" + user.name + " pending visual implementation.");
      //navigate to reservations page with user id as parameter
  }
}