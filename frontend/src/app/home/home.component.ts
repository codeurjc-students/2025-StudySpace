import { Component, OnInit } from '@angular/core';
import { RoomsService } from '../services/rooms.service';
import { RoomDTO } from '../dtos/room.dto';
import { LoginService } from '../login/login.service';
import { Page } from '../dtos/page.model';

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
    //to see the rooms when component starts
    /*this.roomsService.getRooms().subscribe({
      next: (data) => {
        this.rooms = data;
        console.log('Load Rooms:', this.rooms);
      },
      error: (err) => {
        console.error('Error loading rooms:', err);
      }
    });*/
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


  getPagesArray(): number[] {
    return Array.from({ length: this.pageData?.totalPages || 0 }, (_, i) => i);
  }


}
