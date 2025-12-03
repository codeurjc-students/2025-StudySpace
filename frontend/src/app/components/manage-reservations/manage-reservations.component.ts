import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { RoomsService } from '../../services/rooms.service';
import { RoomDTO } from '../../dtos/room.dto';

@Component({
  selector: 'app-manage-reservations',
  templateUrl: './manage-reservations.component.html',
  styleUrls: ['./manage-reservations.component.css']
})
export class ManageReservationsComponent implements OnInit {

  reservations: any[] = [];
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
      this.loadReservations();
    }
    
    //loadRooms
    this.roomsService.getRooms().subscribe(data => this.rooms = data);
  }

  loadReservations() {
    if (this.userId) {
      this.reservationService.getReservationsByUser(this.userId).subscribe({
        next: (data) => this.reservations = data,
        error: (e) => console.error(e)
      });
    }
  }

  deleteReservation(id: number) {
    if(confirm("Are you sure you want to delete this reservation?")) {
      this.reservationService.deleteReservation(id).subscribe(() => this.loadReservations());
    }
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
          this.loadReservations();
        },
        error: () => alert("Update error")
      });
    }
  }
}