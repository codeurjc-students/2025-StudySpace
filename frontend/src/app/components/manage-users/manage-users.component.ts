import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { LoginService } from '../../login/login.service';
import { RoomsService } from '../../services/rooms.service';
import { UserDTO } from '../../dtos/user.dto';
import { RoomDTO } from '../../dtos/room.dto';
import { Page } from '../../dtos/page.model';
import { PaginationUtil } from '../../utils/pagination.util';
import { DialogService } from '../../services/dialog.service';

@Component({
  selector: 'app-manage-users',
  templateUrl: './manage-users.component.html',
  styleUrls: ['./manage-users.component.css'],
})
export class ManageUsersComponent implements OnInit {
  users: UserDTO[] = [];
  pageData?: Page<UserDTO>;
  currentPage: number = 0;

  //for intelligent search
  public searchText: string = '';
  public filterBlocked: string = '';
  public filterType: string = '';
  public filterRoom: string = '';
  public filterDate: string = '';
  public isSearching: boolean = false;
  public availableRooms: RoomDTO[] = [];

  constructor(
    private readonly userService: UserService,
    private readonly loginService: LoginService,
    private readonly roomsService: RoomsService,
    private readonly router: Router,
    private readonly dialogService: DialogService,
  ) {}

  ngOnInit(): void {
    this.loadUsers(0);
  }

  getVisiblePages(): number[] {
    return PaginationUtil.getVisiblePages(this.pageData, this.currentPage);
  }

  toggleAdmin(user: UserDTO) {
    const isNowAdmin = !user.roles.includes('ADMIN'); //if then was admin, now not and viceversa
    this.userService
      .changeRole(user.id, isNowAdmin)
      .subscribe(() => this.loadUsers(this.currentPage));
  }

  toggleBlock(user: UserDTO) {
    this.userService
      .toggleBlock(user.id)
      .subscribe(() => this.loadUsers(this.currentPage));
  }

  deleteUser(user: UserDTO) {
    this.dialogService
      .confirm(
        'Delete User',
        `Are you sure you want to delete user ${user.name}?`,
      )
      .then((confirmed) => {
        if (confirmed) {
          this.userService.deleteUser(user.id).subscribe({
            next: () => {
              this.loadUsers(this.currentPage);
            },
            error: (err) => {
              console.error(err);
              this.dialogService.alert('Error', 'Error deleting user');
            },
          });
        }
      });
  }

  //to see bookings
  viewReservations(user: UserDTO) {
    this.router.navigate(['/admin/users', user.id, 'reservations']);
  }

  onRoomSearchChange() {
    if (!this.filterRoom) {
      this.availableRooms = [];
      return;
    }
    this.roomsService.searchRooms(this.filterRoom).subscribe({
      next: (data) => (this.availableRooms = data.content),
      error: (e) => console.error(e),
    });
  }

  loadUsers(page: number) {
    if (this.isSearching) {
      this.userService
        .searchUsers(
          this.searchText,
          this.filterBlocked,
          this.filterType,
          this.filterRoom,
          this.filterDate,
          page,
        )
        .subscribe({
          next: (data) => {
            this.pageData = data;
            const currentUserId = this.loginService.currentUser?.id;
            this.users = data.content.filter(
              (user) => user.id !== currentUserId,
            );
            this.currentPage = data.number;
          },
        });
    } else {
      this.userService.getUsers(page).subscribe({
        next: (data) => {
          this.pageData = data;
          const currentUserId = this.loginService.currentUser?.id;
          this.users = data.content.filter((user) => user.id !== currentUserId);
          this.currentPage = data.number;
        },
        error: (err) => console.error(err),
      });
    }
  }

  onSearch() {
    if (
      !this.searchText &&
      !this.filterBlocked &&
      !this.filterType &&
      !this.filterRoom &&
      !this.filterDate
    ) {
      this.clearSearch();
      return;
    }

    this.isSearching = true;
    this.loadUsers(0);
  }

  clearSearch() {
    this.searchText = '';
    this.filterBlocked = '';
    this.filterType = '';
    this.filterRoom = '';
    this.filterDate = '';
    this.isSearching = false;
    this.availableRooms = [];
    this.loadUsers(0);
  }
}
