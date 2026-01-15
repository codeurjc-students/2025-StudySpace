import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { RoomDTO } from '../../dtos/room.dto';
import { ReservationLogic } from '../../utils/reservation-logic.util';

@Component({
  selector: 'app-reservation-form',
  templateUrl: './reservation-form.component.html',
  styleUrls: ['./reservation-form.component.css']
})
export class ReservationFormComponent implements OnInit {

  rooms: RoomDTO[] = [];
  roomId: number | null = null;
  reason: string = '';

  selectedDate: string = ''; 
  minDate: string = ''; 
  
  startTimes: string[] = [];
  endTimes: string[] = [];
  
  selectedStartTime: string = '';
  selectedEndTime: string = '';

  occupiedSlots: any[] = [];

  constructor(
    private router: Router,
    private reservationService: ReservationService,
    private roomsService: RoomsService
  ) {}

  ngOnInit(): void {
    //YYYY-MM-DD
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0];

    this.roomsService.getRooms(0, 1000).subscribe({
      next: (data) => {
        this.rooms = data.content.filter((r: RoomDTO) => r.active);
        if (this.rooms.length > 0) {
          this.roomId = this.rooms[0].id;
        }
      },
      error: (err) => console.error('Error loading rooms', err)
    });
  }

  onConfigChange() {
    this.selectedStartTime = '';
    this.selectedEndTime = '';
    this.startTimes = [];
    this.endTimes = [];

    if (this.roomId && this.selectedDate) {
      this.reservationService.checkAvailability(this.roomId, this.selectedDate).subscribe({
        next: (data) => {
          this.occupiedSlots = data;
          this.calculateStartTimes();
        },
        error: (e) => console.error("Error checking availability", e)
      });
    }
  }

  
  calculateStartTimes() {
    this.startTimes = ReservationLogic.generateStartTimes(this.occupiedSlots);
  }

  //calculate end date by the selected start date
  onStartTimeChange() {
    this.selectedEndTime = '';
    this.endTimes = ReservationLogic.generateEndTimes(this.selectedStartTime, this.occupiedSlots);
  }

  onSubmit() {
    if (this.roomId && this.selectedDate && this.selectedStartTime && this.selectedEndTime) {
      //ISO: YYYY-MM-DDTHH:mm:00
      const start = new Date(`${this.selectedDate}T${this.selectedStartTime}:00`);
      const end = new Date(`${this.selectedDate}T${this.selectedEndTime}:00`);

      this.reservationService.createReservation(this.roomId, start, end, this.reason).subscribe({
        next: () => {
          alert('Reservation successfully created!');
          this.router.navigate(['/']);
        },
        error: (err) => {
          console.error(err);
          alert(err.error || 'Error creating reservation'); 
        }
      });
    }
  }



}