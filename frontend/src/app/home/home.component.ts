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


  getVisiblePages(): number[] {
    if (!this.pageData) return [];

    const totalPages = this.pageData.totalPages;
    const maxPagesToShow = 10; // Cantidad máxima de botones a mostrar

    //less pages than the maximum, show all pages
    if (totalPages <= maxPagesToShow) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    // many pages calculate the start and end
    let startPage = this.currentPage - Math.floor(maxPagesToShow / 2);
    let endPage = this.currentPage + Math.ceil(maxPagesToShow / 2);

    // Ajuste si nos salimos por el principio (ej: estamos en la pág 1)
    if (startPage < 0) {
      startPage = 0;
      endPage = maxPagesToShow;
    }

    // Ajuste si nos salimos por el final (ej: estamos en la última pág)
    if (endPage > totalPages) {
      endPage = totalPages;
      startPage = totalPages - maxPagesToShow;
    }

    // Generamos el array de números
    const pages = [];
    for (let i = startPage; i < endPage; i++) {
      pages.push(i);
    }
    return pages;
  }


}
