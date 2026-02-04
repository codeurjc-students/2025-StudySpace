import { Component,OnInit } from '@angular/core';
import { RoomsService } from '../../services/rooms.service'; 
import { RoomDTO } from '../../dtos/room.dto';
import { Page } from '../../dtos/page.model';
import { PaginationUtil } from '../../utils/pagination.util';

@Component({
  selector: 'app-manage-rooms',
  templateUrl: './manage-rooms.component.html',
  styleUrl: './manage-rooms.component.css'
})
export class ManageRoomsComponent implements OnInit {
  rooms: RoomDTO[] = [];
  pageData?: Page<RoomDTO>;
  currentPage: number = 0;

  constructor(private readonly roomsService: RoomsService) { }

  ngOnInit(): void {
    this.loadRooms(0);
  }

  loadRooms(page: number) {
    this.roomsService.getRooms(page).subscribe({
      next: (data) => {
        this.pageData = data;
        this.rooms = data.content; 
        this.currentPage = data.number;
      },
      error: (err) => console.error('Error loading rooms', err)
    });
  }

  getVisiblePages(): number[] {
    return PaginationUtil.getVisiblePages(this.pageData, this.currentPage);
  }
  
  deleteRoom(id: number) {
    
    const reason = prompt("⚠️⚠️ You are going to delete this room and cancel ALL its future bookings permanently.⚠️⚠️\n Be sure after making this action because this action cannot be undone.\n\n Please write the reason to notify affected users by email:");
    if (reason === null) return;
      this.roomsService.deleteRoom(id,reason).subscribe({
        next: () => {
          this.rooms = this.rooms.filter(room => room.id !== id);
          alert("Classroom successfully removed.✅");
          this.loadRooms(this.currentPage);
        },
        error: (err) => {
          console.error("Error deleting:", err);
          //alert("❌ Error deleting the classroom. It may have associated reservations. ❌");
        }
      });
    
  }
}
