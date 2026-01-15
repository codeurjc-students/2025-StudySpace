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
    /*const times: string[] = [];
    const now = new Date();
    //if today filter hour slots already passed
    const isToday = this.selectedDate === this.minDate; 
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
        
        if (!this.isTimeOccupied(timeStr)) {
          times.push(timeStr);
        }
      }
    }
    this.startTimes = times;*/
  }

  //calculate end date by the selected start date
  onStartTimeChange() {
    this.selectedEndTime = '';
    this.endTimes = ReservationLogic.generateEndTimes(this.selectedStartTime, this.occupiedSlots);
  
    /*
    this.endTimes = [];
    
    if (!this.selectedStartTime) return;

    const [startH, startM] = this.selectedStartTime.split(':').map(Number);
    const startTotalMins = startH * 60 + startM;

    let h = startH;
    let m = startM + 30;
    if (m === 60) { h++; m = 0; }

    while (h < 21 || (h === 21 && m === 0)) {
        const currentTotalMins = h * 60 + m;
        const duration = currentTotalMins - startTotalMins;

        
        if (duration > 180) {//more than 3 hours that day
            break;
        }

        const timeStr = `${this.pad(h)}:${this.pad(m)}`;
        
        if (this.isOverlap(this.selectedStartTime, timeStr)) {//already a reservation
            break; 
        }

        this.endTimes.push(timeStr);

        m += 30;
        if (m === 60) { h++; m = 0; }
    }*/
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


  pad(n: number): string {
    return n < 10 ? '0' + n : '' + n;
  }

  //see if it coincides with any existing reservation
  isTimeOccupied(timeStr: string): boolean {
    const timeMins = this.toMinutes(timeStr);

    return this.occupiedSlots.some(res => {
      const resStart = new Date(res.startDate);
      const resEnd = new Date(res.endDate);
      
      const startMins = resStart.getHours() * 60 + resStart.getMinutes();
      const endMins = resEnd.getHours() * 60 + resEnd.getMinutes();

      //time == end, it is free
      return timeMins >= startMins && timeMins < endMins;
    });
  }

  //if it overlaps with any existing reservation
  isOverlap(startStr: string, endStr: string): boolean {
    const s = this.toMinutes(startStr);
    const e = this.toMinutes(endStr);

    return this.occupiedSlots.some(res => {
      const resStart = new Date(res.startDate);
      const resEnd = new Date(res.endDate);
      const rS = resStart.getHours() * 60 + resStart.getMinutes();
      const rE = resEnd.getHours() * 60 + resEnd.getMinutes();

      return e > rS && s < rE;
    });
  }

  toMinutes(time: string): number {
    const [h, m] = time.split(':').map(Number);
    return h * 60 + m;
  }
}