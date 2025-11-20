import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router'; // Para leer el ID de la URL
import { RoomsService } from '../../services/rooms.service'; // Ajusta la ruta si es necesario
import { RoomDTO } from '../../dtos/room.dto';

@Component({
  selector: 'app-room-detail',
  templateUrl: './room-detail.component.html',
  styleUrls: ['./room-detail.component.css']
})
export class RoomDetailComponent implements OnInit {

  room: RoomDTO | undefined;

  constructor(
    private route: ActivatedRoute,
    private roomsService: RoomsService
  ) {}

  ngOnInit(): void {
    // Leemos el parámetro 'id' de la URL
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.roomsService.getRoom(id).subscribe({
        next: (data) => this.room = data,
        error: (err) => console.error(err)
      });
    }
  }
}