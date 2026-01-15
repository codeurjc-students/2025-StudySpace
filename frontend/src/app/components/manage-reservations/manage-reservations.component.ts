import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { RoomDTO } from '../../dtos/room.dto';
import { Page } from '../../dtos/page.model';
import { PaginationUtil } from '../../utils/pagination.util';
import { ReservationLogic } from '../../utils/reservation-logic.util';

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

  editDateStr: string = '';
  editStartTime: string = '';
  editEndTime: string = '';
  
  availableStartTimes: string[] = [];
  availableEndTimes: string[] = [];
  occupiedSlots: any[] = [];
  minDate: string = '';

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
    this.minDate = new Date().toISOString().split('T')[0];
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
    this.editingReservation = { ...reservation }; 
    const startDate = new Date(reservation.startDate);
    const endDate = new Date(reservation.endDate);

    this.editDateStr = startDate.toISOString().split('T')[0];
    this.editStartTime = ReservationLogic.pad(startDate.getHours()) + ':' + ReservationLogic.pad(startDate.getMinutes());
    this.editEndTime = ReservationLogic.pad(endDate.getHours()) + ':' + ReservationLogic.pad(endDate.getMinutes());

    this.onConfigChange(true);
  }

  cancelEdit() {
    this.editingReservation = null;
    this.occupiedSlots = [];
  
  }

  saveEdit() {
    if (this.editingReservation) {
      const newStart = new Date(`${this.editDateStr}T${this.editStartTime}:00`);
      const newEnd = new Date(`${this.editDateStr}T${this.editEndTime}:00`);

      this.editingReservation.startDate = newStart;
      this.editingReservation.endDate = newEnd;

      this.reservationService.updateReservation(this.editingReservation.id, this.editingReservation).subscribe({
        next: () => {
          alert("Booking updated successfully");
          this.editingReservation = null;
          this.loadReservations(this.currentPage);
        },
        error: (err) => {
            console.error(err);
            alert("Update error: " + (err.error?.message || "Check times"));
        }
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



  onConfigChange(isInit: boolean = false) {
    if (!isInit) {
        this.availableStartTimes = [];
        this.availableEndTimes = [];
        if (!this.editingReservation.roomId || !this.editDateStr) return;
    }

    const roomId = this.editingReservation.roomId;
    const date = this.editDateStr;

    this.reservationService.checkAvailability(roomId, date).subscribe(data => {
        this.occupiedSlots = data.filter((r: any) => r.id !== this.editingReservation.id);
        
        this.availableStartTimes = ReservationLogic.generateStartTimes(this.occupiedSlots);

        if (!isInit && !this.availableStartTimes.includes(this.editStartTime)) {
             this.editStartTime = '';
             this.editEndTime = '';
        } else {
             this.onStartTimeChange(isInit ? this.editEndTime : null);
        }
    });
  }


  onStartTimeChange(preselectedEndTime: string | null = null) {
    this.availableEndTimes = [];
    if (!this.editStartTime) return;

    this.availableEndTimes = ReservationLogic.generateEndTimes(this.editStartTime, this.occupiedSlots);

    if (preselectedEndTime) {
        if (this.availableEndTimes.includes(preselectedEndTime)) {
            this.editEndTime = preselectedEndTime;
        } else {
            this.editEndTime = '';
        }
    } else {
        this.editEndTime = '';
    }
  }


  

}