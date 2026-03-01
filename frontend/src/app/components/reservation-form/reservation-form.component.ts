import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { RoomDTO } from '../../dtos/room.dto';
import { ReservationLogic } from '../../utils/reservation-logic.util';

@Component({
  selector: 'app-reservation-form',
  templateUrl: './reservation-form.component.html',
  styleUrls: ['./reservation-form.component.css'],
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

  public roomSearchText: string = '';
  public selectedCampus: string = '';
  public minCapacity: number | null = null;
  public visibleRooms: RoomDTO[] = [];

  constructor(
    private router: Router,
    private reservationService: ReservationService,
    private roomsService: RoomsService,
  ) {}

  ngOnInit(): void {
    //YYYY-MM-DD
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0];

    this.searchRooms();
  }

  onConfigChange() {
    this.selectedStartTime = '';
    this.selectedEndTime = '';
    this.startTimes = [];
    this.endTimes = [];

    if (this.roomId && this.selectedDate) {
      this.reservationService
        .checkAvailability(this.roomId, this.selectedDate)
        .subscribe({
          next: (data) => {
            this.occupiedSlots = data;
            this.calculateStartTimes();
          },
          error: (e) => console.error('Error checking availability', e),
        });
    }
  }

  calculateStartTimes() {
    const rawStartTimes = ReservationLogic.generateStartTimes(
      this.occupiedSlots,
    );
    const now = new Date(); //actual hour

    this.startTimes = rawStartTimes.filter((time) => {
      const slotDateTime = new Date(`${this.selectedDate}T${time}:00`);

      return slotDateTime > now;
    });
  }

  //calculate end date by the selected start date
  onStartTimeChange() {
    this.selectedEndTime = '';
    this.endTimes = ReservationLogic.generateEndTimes(
      this.selectedStartTime,
      this.occupiedSlots,
    );
  }

  onSubmit() {
    if (
      this.roomId &&
      this.selectedDate &&
      this.selectedStartTime &&
      this.selectedEndTime
    ) {
      //ISO: YYYY-MM-DDTHH:mm:00
      const start = new Date(
        `${this.selectedDate}T${this.selectedStartTime}:00`,
      );
      const end = new Date(`${this.selectedDate}T${this.selectedEndTime}:00`);

      this.reservationService
        .createReservation(this.roomId, start, end, this.reason)
        .subscribe({
          next: () => {
            alert(
              'Reservation successfully created! Now its time to verify it or it will be automatically cancelled 1 hour before the start time',
            );
            this.router.navigate(['/']);
          },
          error: (err) => {
            console.error(err);
            alert(err.error || 'Error creating reservation');
          },
        });
    }
  }

  selectRoom(room: RoomDTO) {
    this.roomId = room.id;
    this.onConfigChange();
  }

  searchRooms() {
    this.roomsService
      .searchRooms(
        this.roomSearchText,
        this.minCapacity || undefined,
        this.selectedCampus || undefined,
        true,
        0,
        100,
      )
      .subscribe({
        next: (data) => {
          this.visibleRooms = data.content;
          if (
            this.roomId &&
            !this.visibleRooms.find((r) => r.id === this.roomId)
          ) {
            // falta rellenar
          }
        },
        error: (e) => console.error(e),
      });
  }

  clearRoomSearch() {
    this.roomSearchText = '';
    this.selectedCampus = '';
    this.minCapacity = null;
    this.searchRooms();
  }
}
