import { Component, OnInit } from '@angular/core';
import { RoomsService } from '../services/rooms.service';
import { RoomDTO } from '../dtos/room.dto';
import { LoginService } from '../login/login.service';
import { Page } from '../dtos/page.model';
import { PaginationUtil } from '../utils/pagination.util';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  //hera are save the rooms
  public rooms: RoomDTO[] = [];
  public pageData?: Page<RoomDTO>;
  public currentPage: number = 0;

  constructor(private readonly roomsService: RoomsService,
    public loginService: LoginService
  ) {}

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.roomsService.getRooms(page).subscribe({
      next: (data) => {
        this.pageData = data;
        this.rooms = data.content;
        this.currentPage = data.number;
      },
      error: (err) => console.error('Error loading rooms:', err)
    });
  }


  getVisiblePages(): number[] {
    return PaginationUtil.getVisiblePages(this.pageData, this.currentPage);
  }


}
