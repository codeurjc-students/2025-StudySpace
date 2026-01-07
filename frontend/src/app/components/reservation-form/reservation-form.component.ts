import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service'; 
import { RoomDTO } from '../../dtos/room.dto';

@Component({
  selector: 'app-reservation-form',
  templateUrl: './reservation-form.component.html',
  styleUrls: ['./reservation-form.component.css']
})
export class ReservationFormComponent implements OnInit {

  
  rooms: RoomDTO[] = [];
  roomId: number | null = null;
  
  startDate: string = '';
  endDate: string = '';
  reason: string = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly reservationService: ReservationService,
    private readonly roomsService: RoomsService
  ) {}

  ngOnInit(): void {
    //we obtain the roomId from the route parameters
   this.roomsService.getRooms(0,1000).subscribe({//for the moment we get a lot of rooms to avoid not seen some rooms
        next: (data) => {
          //only active rooms
          this.rooms = data.content.filter((room:RoomDTO) => room.active === true);
          if (this.rooms.length > 0) {
              this.roomId = this.rooms[0].id;
          } else {
              this.roomId = null; // no avaliable rooms
          }
        },
        error: (err) => console.error('Error loading rooms', err)
    });
  }

  onSubmit() {
    if (this.roomId && this.startDate && this.endDate) {
      //transform to Date objects the string inputs
      const start = new Date(this.startDate);
      const end = new Date(this.endDate);

      this.reservationService.createReservation(this.roomId, start, end, this.reason).subscribe({
        next: (res) => {
          alert('Reservation successfully created!');
          // send to the profile where the user can see their reservations
          this.router.navigate(['/']); 
        },
        error: (err) => {
          console.error(err);
          alert('Error creating reservation. Please try again.');
        }
      });
    } else {
        alert('Please fill in the dates.');
    }
  }
}