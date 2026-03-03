import { Component, OnInit } from '@angular/core';
import { RoomsService } from '../../services/rooms.service';
import { RoomDTO } from '../../dtos/room.dto';
import { Page } from '../../dtos/page.model';
import { PaginationUtil } from '../../utils/pagination.util';

@Component({
  selector: 'app-manage-rooms',
  templateUrl: './manage-rooms.component.html',
})
export class ManageRoomsComponent implements OnInit {
  rooms: RoomDTO[] = [];
  pageData?: Page<RoomDTO>;
  currentPage: number = 0;

  //for search filter algorithm
  public searchText: string = '';
  public selectedCampus: string = '';
  public filterActive: string = '';
  public minCapacity: number | null = null;
  public isSearching: boolean = false;

  constructor(private readonly roomsService: RoomsService) {}

  ngOnInit(): void {
    this.loadRooms(0);
  }

  loadRooms(page: number) {
    if (this.isSearching) {
      let activeParam: boolean | undefined = undefined;
      if (this.filterActive === 'true') activeParam = true;
      if (this.filterActive === 'false') activeParam = false;

      this.roomsService
        .searchRooms(
          this.searchText,
          this.minCapacity || undefined,
          this.selectedCampus || undefined,
          activeParam,
          page,
        )
        .subscribe({
          next: (data) => {
            this.pageData = data;
            this.rooms = data.content;
            this.currentPage = data.number;
          },
        });
    } else {
      this.roomsService.getRooms(page).subscribe({
        next: (data) => {
          this.pageData = data;
          this.rooms = data.content;
          this.currentPage = data.number;
        },
        error: (err) => console.error('Error loading rooms', err),
      });
    }
  }

  getVisiblePages(): number[] {
    return PaginationUtil.getVisiblePages(this.pageData, this.currentPage);
  }

  deleteRoom(id: number) {
    const reason = prompt(
      '⚠️⚠️ You are going to delete this room and cancel ALL its future bookings permanently.⚠️⚠️\n Be sure after making this action because this action cannot be undone.\n\n Please write the reason to notify affected users by email:',
    );
    if (reason === null) return;
    this.roomsService.deleteRoom(id, reason).subscribe({
      next: () => {
        this.rooms = this.rooms.filter((room) => room.id !== id);
        alert('Classroom successfully removed.✅');
        this.loadRooms(this.currentPage);
      },
      error: (err) => {
        console.error('Error deleting:', err);
      },
    });
  }

  onSearch() {
    if (
      !this.searchText &&
      !this.selectedCampus &&
      !this.minCapacity &&
      !this.filterActive
    ) {
      this.clearSearch();
      return;
    }
    this.isSearching = true;
    this.loadRooms(0);
  }

  clearSearch() {
    this.searchText = '';
    this.selectedCampus = '';
    this.minCapacity = null;
    this.filterActive = ''; 
    
    this.isSearching = false;
    this.loadRooms(0);
  }
}
