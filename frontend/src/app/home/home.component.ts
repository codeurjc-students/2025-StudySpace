import { Component, OnInit } from '@angular/core';
import { RoomsService } from '../services/rooms.service';
import { RoomDTO } from '../dtos/room.dto';
import { LoginService } from '../login/login.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  //hera are save the rooms
  public rooms: RoomDTO[] = [];

  constructor(private readonly roomsService: RoomsService,
    public loginService: LoginService
  ) {}

  ngOnInit(): void {
    //to see the rooms when component starts
    this.roomsService.getRooms().subscribe({
      next: (data) => {
        this.rooms = data;
        console.log('Aulas cargadas:', this.rooms);
      },
      error: (err) => {
        console.error('Error al cargar aulas:', err);
      }
    });
  }
}
