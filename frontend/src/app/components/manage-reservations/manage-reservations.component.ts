import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { RoomDTO } from '../../dtos/room.dto';
import { Page } from '../../dtos/page.model';
import { PaginationUtil } from '../../utils/pagination.util';

@Component({
  selector: 'app-manage-reservations',
  templateUrl: './manage-reservations.component.html',
  styleUrls: ['./manage-reservations.component.css']
})
export class ManageReservationsComponent implements OnInit {

  reservations: any[] = [];
  pageData?: Page<any>;
  currentPage: number = 0;
  userId: number | null = null;
  
  
  editingReservation: any = null; // actually edited
  rooms: RoomDTO[] = []; 

  constructor(
    private readonly route: ActivatedRoute,
    private readonly reservationService: ReservationService,
    private readonly roomsService: RoomsService
  ) {}

  ngOnInit(): void {
    //id from the url
    const id = this.route.snapshot.paramMap.get('userId');
    if (id) {
      this.userId = +id;
      this.loadReservations(0);
    }
    
    //loadRooms
    this.roomsService.getRooms(0, 100).subscribe(data => this.rooms = data.content);
  }

  loadReservations(page: number) {
    if (this.userId) {
      this.reservationService.getReservationsByUser(this.userId, page).subscribe({
        next: (data) => {
            this.pageData = data;
            this.reservations = data.content;
            this.currentPage = data.number;
        },
        error: (e) => console.error("Error loading reservations",e)
      });
    }
  }

  deleteReservation(id: number) {
    if(confirm("Are you sure you want to delete this reservation?")) {
      this.reservationService.deleteReservation(id).subscribe({
        next:() => {
          alert("Reservation deleted successfully.");
          this.loadReservations(this.currentPage)
        },
        error: () => alert("Error deleting")
    });
    }
  }


  getVisiblePages(): number[] {
    return PaginationUtil.getVisiblePages(this.pageData, this.currentPage);
  }
  


  startEdit(reservation: any) {
   //copy for editing without modifying original until saved
    this.editingReservation = { ...reservation }; 
    
    //take care that roomId has a value and is not and object
    if(this.editingReservation.room) {
        this.editingReservation.roomId = this.editingReservation.room.id;
    }
  }

  cancelEdit() {
    this.editingReservation = null;
  }

  saveEdit() {
    if (this.editingReservation) {
      this.reservationService.updateReservation(this.editingReservation.id, this.editingReservation).subscribe({
        next: () => {
          alert("Booking updated successfully");
          this.editingReservation = null;
          this.loadReservations(this.currentPage);
        },
        error: () => alert("Update error")
      });
    }
  }

  isReservationActive(res: any): boolean {
    if (!res || !res.endDate) return false;
    const now = new Date();
    const end = new Date(res.endDate);
    return !res.cancelled && end > now;
  }

  performCancel(id: number) {
    if (confirm("Are you sure you want to cancel this reservation?")) {
      this.reservationService.cancelReservation(id).subscribe({
        next: () => {
          alert("Reservation successfully cancelled.");
          this.loadReservations(this.currentPage); 
        },
        error: (err) => {
          console.error(err);
          alert("Cancellation error:");
        }
      });
    }
  }


}