import { Component,OnInit } from '@angular/core';
import { RoomsService } from '../../services/rooms.service'; 
import { RoomDTO } from '../../dtos/room.dto';

@Component({
  selector: 'app-manage-rooms',
  templateUrl: './manage-rooms.component.html',
  styleUrl: './manage-rooms.component.css'
})
export class ManageRoomsComponent implements OnInit {
  rooms: RoomDTO[] = [];

  constructor(private readonly roomsService: RoomsService) { }

  ngOnInit(): void {
    this.loadRooms();
  }

  loadRooms() {
    this.roomsService.getRooms().subscribe({
      next: (data) => {
        this.rooms = data;
      },
      error: (err) => console.error('Error loading rooms', err)
    });
  }

  
  deleteRoom(id: number) {
    const confirmDelete = confirm("⚠️⚠️ Are you sure you want to delete this classroom permanently?⚠️⚠️\n\n This action cannot be undone.");
    
    if (confirmDelete) {
      this.roomsService.deleteRoom(id).subscribe({
        next: () => {
          this.rooms = this.rooms.filter(room => room.id !== id);
          alert("Classroom successfully removed.✅");
        },
        error: (err) => {
          console.error("Error deleting:", err);
          alert("❌ Error deleting the classroom. It may have associated reservations. ❌");
        }
      });
    }
  }
}
