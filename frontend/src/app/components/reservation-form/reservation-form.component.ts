import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { RoomDTO } from '../../dtos/room.dto';
import { ReservationLogic } from '../../utils/reservation-logic.util';
import { DialogService } from '../../services/dialog.service';

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

  roomSearchText: string = '';
  selectedCampus: string = '';
  minCapacity: number | null = null;
  visibleRooms: RoomDTO[] = [];

  desiredStartTime: string = '';
  desiredEndTime: string = '';
  smartSuggestions: any[] = [];
  smartSearchLoading: boolean = false;

  allPossibleTimes: string[] = [];

  constructor(
    private readonly router: Router,
    private readonly reservationService: ReservationService,
    private readonly roomsService: RoomsService,
    private readonly dialogService: DialogService,
  ) {}

  ngOnInit(): void {
    //YYYY-MM-DD
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0];

    this.searchRooms();
    this.generateAllPossibleTimes();
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
    const now = new Date(); // current hour

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
            this.dialogService.alert(
              'Success',
              'Reservation successfully created! We have sent a confirmation email with the calendar details attached.'
            ).then(() => {
              this.router.navigate(['/']);
            });
          },
          error: (err) => {
            console.error(err);
            this.dialogService.alert(
              'Error',
              err.error || 'Error creating reservation',
            );
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
            !this.visibleRooms.some((r) => r.id === this.roomId)
          ) {
            this.roomId = null;
            this.onConfigChange();
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

  triggerSmartSearch() {
    if (!this.selectedDate || !this.desiredStartTime || !this.desiredEndTime)
      return;
    this.smartSearchLoading = true;
    this.smartSuggestions = [];

    const start = new Date(`${this.selectedDate}T${this.desiredStartTime}:00`);
    const end = new Date(`${this.selectedDate}T${this.desiredEndTime}:00`);

    const targetRoom = this.visibleRooms.find((r) => r.id === this.roomId);
    const targetCampus = targetRoom
      ? targetRoom.camp
      : this.selectedCampus || undefined;

    this.reservationService
      .smartSearch(start, end, this.minCapacity || undefined, targetCampus)
      .subscribe({
        next: (data) => {
          this.smartSuggestions = data;
          this.smartSearchLoading = false;
          if (data.length === 0) {
            this.dialogService.alert(
              'Error',
              'No alternatives found. Try changing the date or your filters.',
            );
          }
        },
        error: (err) => {
          console.error(err);
          this.smartSearchLoading = false;
        },
      });
  }

  applySuggestion(sug: any) {
    this.roomId = sug.room.id;

    const suggestedStartDate = new Date(sug.suggestedStart);
    const suggestedEndDate = new Date(sug.suggestedEnd);

    this.selectedDate = suggestedStartDate.toISOString().split('T')[0];
    this.onConfigChange();

    const startHH = String(suggestedStartDate.getHours()).padStart(2, '0');
    const startMM = String(suggestedStartDate.getMinutes()).padStart(2, '0');
    const endHH = String(suggestedEndDate.getHours()).padStart(2, '0');
    const endMM = String(suggestedEndDate.getMinutes()).padStart(2, '0');

    setTimeout(() => {
      this.selectedStartTime = `${startHH}:${startMM}`;
      this.onStartTimeChange();
      this.selectedEndTime = `${endHH}:${endMM}`;

      this.smartSuggestions = [];
      this.desiredStartTime = '';
      this.desiredEndTime = '';
    }, 400);
  }

  generateAllPossibleTimes() {
    const times = [];
    for (let h = 8; h <= 20; h++) {
      const hour = h.toString().padStart(2, '0');
      times.push(`${hour}:00`, `${hour}:30`);
    }
    times.push('21:00');
    this.allPossibleTimes = times;
  }
}
