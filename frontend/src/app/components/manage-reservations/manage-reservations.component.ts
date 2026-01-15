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

    this.onConfigChange(true);/*
    this.editingReservation = { ...reservation }; 
    
    if(this.editingReservation.room) {
        this.editingReservation.roomId = this.editingReservation.room.id;
    }

    const startDateObj = new Date(this.editingReservation.startDate);
    const endDateObj = new Date(this.editingReservation.endDate);

    //YYYY-MM-DD
    this.editDateStr = startDateObj.toISOString().split('T')[0];
    
    //HH:mm
    this.editStartTime = this.formatTime(startDateObj);
    this.editEndTime = this.formatTime(endDateObj);


    this.onConfigChange(true); //true no delete selecctions^*/
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
        // Solo reseteamos si el usuario cambia algo, no al iniciar edición
        if (!this.editingReservation.roomId || !this.editDateStr) return;
    }

    const roomId = this.editingReservation.roomId;
    const date = this.editDateStr;

    this.reservationService.checkAvailability(roomId, date).subscribe(data => {
        // FILTRAR: Quitamos nuestra propia reserva de la lista de ocupadas
        this.occupiedSlots = data.filter((r: any) => r.id !== this.editingReservation.id);
        
        // USAMOS UTILIDAD
        this.availableStartTimes = ReservationLogic.generateStartTimes(this.occupiedSlots);

        // Si no es init, o si la hora actual ya no es válida, reseteamos
        if (!isInit && !this.availableStartTimes.includes(this.editStartTime)) {
             this.editStartTime = '';
             this.editEndTime = '';
        } else {
             // Recalcular horas fin disponibles basado en la hora inicio seleccionada
             this.onStartTimeChange(isInit ? this.editEndTime : null);
        }
    });
    /*if (!isInit) {
        this.editStartTime = '';
        this.editEndTime = '';
    }
    this.availableStartTimes = [];
    this.availableEndTimes = [];

    if (this.editingReservation.roomId && this.editDateStr) {
      this.reservationService.checkAvailability(this.editingReservation.roomId, this.editDateStr).subscribe({
        next: (data) => {
          //the own reservation out the list of ocupied
          this.occupiedSlots = data.filter(res => res.id !== this.editingReservation.id);
          
          this.calculateStartTimes();
          
          if (isInit && this.editStartTime) {
            this.onStartTimeChange(this.editEndTime);
          }
        },
        error: (e) => console.error("Error checking availability", e)
      });
    }*/
  }

  calculateStartTimes() {
    const times: string[] = [];
    const now = new Date();
    const isToday = this.editDateStr === this.minDate; 
    const currentHour = now.getHours();
    const currentMin = now.getMinutes();

    for (let h = 8; h < 21; h++) {
      for (let m of [0, 30]) {
        if (h === 21) continue; 

        if (isToday) {
          if (h < currentHour || (h === currentHour && m <= currentMin)) {
            continue;
          }
        }

        const timeStr = `${this.pad(h)}:${this.pad(m)}`;
        
        //colision?
        if (!this.isTimeOccupied(timeStr)) {
          times.push(timeStr);
        }
      }
    }
    this.availableStartTimes = times;
    
    //if is not valid durring edition, clear it
    if (!this.availableStartTimes.includes(this.editStartTime)) {//for future implementations, maybe delete it
        this.editStartTime = '';
        this.editEndTime = '';
        this.availableEndTimes = [];
    }
  }

  onStartTimeChange(preselectedEndTime: string | null = null) {
    this.availableEndTimes = [];
    if (!this.editStartTime) return;

    // USAMOS UTILIDAD
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
    /*if (!preselectedEndTime) {
      this.editEndTime = '';
    }
    this.availableEndTimes = [];
    
    if (!this.editStartTime) return;

    const [startH, startM] = this.editStartTime.split(':').map(Number);
    let h = startH;
    let m = startM + 30;
    if (m === 60) { h++; m = 0; }

    while (h < 21 || (h === 21 && m === 0)) {
      const timeStr = `${this.pad(h)}:${this.pad(m)}`;
      
      if (this.isOverlap(this.editStartTime, timeStr)) {
        break; 
      }
      this.availableEndTimes.push(timeStr);
      
      m += 30;
      if (m === 60) { h++; m = 0; }
    }
    if (preselectedEndTime) {
        if (this.availableEndTimes.includes(preselectedEndTime)) {
            this.editEndTime = preselectedEndTime;
        } else {
            this.editEndTime = '';
        }
    }*/
  }

  pad(n: number): string { return n < 10 ? '0' + n : '' + n; }

  formatTime(date: Date): string {
    return `${this.pad(date.getHours())}:${this.pad(date.getMinutes())}`;
  }

  isTimeOccupied(timeStr: string): boolean {
    const timeMins = this.toMinutes(timeStr);
    return this.occupiedSlots.some(res => {
      const start = new Date(res.startDate);
      const end = new Date(res.endDate);
      const sMins = start.getHours() * 60 + start.getMinutes();
      const eMins = end.getHours() * 60 + end.getMinutes();
      return timeMins >= sMins && timeMins < eMins;
    });
  }


  isOverlap(startStr: string, endStr: string): boolean {
    const s = this.toMinutes(startStr);
    const e = this.toMinutes(endStr);
    return this.occupiedSlots.some(res => {
      const start = new Date(res.startDate);
      const end = new Date(res.endDate);
      const rS = start.getHours() * 60 + start.getMinutes();
      const rE = end.getHours() * 60 + end.getMinutes();
      return e > rS && s < rE;
    });
  }

  toMinutes(time: string): number {
    const [h, m] = time.split(':').map(Number);
    return h * 60 + m;
  }

  

}