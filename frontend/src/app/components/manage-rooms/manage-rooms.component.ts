import { Component,OnInit } from '@angular/core';
import { RoomsService } from '../../services/rooms.service'; 
import { RoomDTO } from '../../dtos/room.dto';
import { Page } from '../../dtos/page.model';

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
    if (!this.pageData) return [];

    const totalPages = this.pageData.totalPages;
    const maxPagesToShow = 10; 


    if (totalPages <= maxPagesToShow) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    let startPage = this.currentPage - Math.floor(maxPagesToShow / 2);
    let endPage = this.currentPage + Math.ceil(maxPagesToShow / 2);

    if (startPage < 0) {
      startPage = 0;
      endPage = maxPagesToShow;
    }

    if (endPage > totalPages) {
      endPage = totalPages;
      startPage = totalPages - maxPagesToShow;
    }

    const pages = [];
    for (let i = startPage; i < endPage; i++) {
      pages.push(i);
    }
    return pages;
  }
  
  deleteRoom(id: number) {
    const confirmDelete = confirm("⚠️⚠️ Are you sure you want to delete this classroom permanently?⚠️⚠️\n\n This action cannot be undone.");
    
    if (confirmDelete) {
      this.roomsService.deleteRoom(id).subscribe({
        next: () => {
          this.rooms = this.rooms.filter(room => room.id !== id);
          alert("Classroom successfully removed.✅");
          this.loadRooms(this.currentPage);
        },
        error: (err) => {
          console.error("Error deleting:", err);
          alert("❌ Error deleting the classroom. It may have associated reservations. ❌");
        }
      });
    }
  }
}
