import { Component, OnInit } from '@angular/core';
import { RoomsService } from '../services/rooms.service';
import { RoomDTO } from '../dtos/room.dto';
import { LoginService } from '../login/login.service';
import { Page } from '../dtos/page.model';
import { CampusService } from '../services/campus.service';
import { CampusDTO } from '../dtos/campus.dto';

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
  public campus: CampusDTO[] = [];
  public selectedCampusId: number | null = null;
  public minCapacity: number | null = null;
  public isSearching: boolean = false;

  constructor(
    private readonly roomsService: RoomsService,
    public loginService: LoginService,
    private readonly campusService: CampusService,
  ) {}

  ngOnInit(): void {
    this.campusService.getAllCampus().subscribe((data) => (this.campus = data));
    this.loadPage(0);
  }

  loadPage(page: number): void {
    if (this.isSearching) {
      this.roomsService
        .searchRooms(
          this.searchText,
          this.minCapacity || undefined,
          this.selectedCampusId || undefined,
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
    if (!this.searchText && !this.selectedCampusId && !this.minCapacity) {
      this.clearSearch();
      return;
    }

    this.isSearching = true;
    this.loadPage(0);
  }

  clearSearch(): void {
    this.searchText = '';
    this.selectedCampusId = null;
    this.minCapacity = null;
    this.isSearching = false;
    this.loadPage(0);
  }
}
