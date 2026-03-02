import { Component, OnInit } from '@angular/core';
import { RoomsService } from '../services/rooms.service';
import { RoomDTO } from '../dtos/room.dto';
import { LoginService } from '../login/login.service';
import { Page } from '../dtos/page.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  //hera are save the rooms
  public rooms: RoomDTO[] = [];
  public pageData?: Page<RoomDTO>;
  public currentPage: number = 0;
  //for search filter
  public searchText: string = '';
  public selectedCampus: string = '';
  public minCapacity: number | null = null;
  public isSearching: boolean = false;

  constructor(
    private readonly roomsService: RoomsService,
    public loginService: LoginService,
  ) {}

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number): void {
    if (this.isSearching) {
      this.roomsService
        .searchRooms(
          this.searchText,
          this.minCapacity || undefined,
          this.selectedCampus || undefined,
          true,
          page,
        )
        .subscribe({
          next: (data) => {
            this.pageData = data;
            this.rooms = data.content;
            this.currentPage = data.number;
          },
          error: (err) => console.error('Error in search:', err),
        });
    } else {
      this.roomsService.getRooms(page).subscribe({
        next: (data) => {
          this.pageData = data;
          this.rooms = data.content;
          this.currentPage = data.number;
        },
        error: (err) => console.error('Error loading rooms:', err),
      });
    }
  }

  onSearch(): void {
    if (!this.searchText && !this.selectedCampus && !this.minCapacity) {
      this.clearSearch();
      return;
    }

    this.isSearching = true;
    this.loadPage(0);
  }

  clearSearch(): void {
    this.searchText = '';
    this.selectedCampus = '';
    this.minCapacity = null;
    this.isSearching = false;
    this.loadPage(0);
  }
}
