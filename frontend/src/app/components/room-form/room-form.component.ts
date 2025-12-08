import { Component,OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { RoomsService } from '../../services/rooms.service';
import { SoftwareService, SoftwareDTO } from '../../services/software.service'; 

@Component({
  selector: 'app-room-form',
  templateUrl: './room-form.component.html',
  styleUrl: './room-form.component.css'
})
export class RoomFormComponent implements OnInit {

  isEditMode: boolean = false; 
  roomId: number | null = null;

  room = {   //defect values
    name: '',
    capacity: 0,
    camp: 'MOSTOLES', 
    place: '',
    coordenades: '' ,
    softwareIds: [] as number[]  // Array to hold selected software IDs
  };

  // Options for campus select, should match with Enum CampusType in backend
  campusOptions = ['ALCORCON', 'MOSTOLES', 'VICALVARO', 'FUENLABRADA', 'QUINTANA'];
  availableSoftware: SoftwareDTO[] = [];

  constructor(
    private readonly roomsService: RoomsService,
    private readonly softwareService: SoftwareService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.softwareService.getAllSoftwares().subscribe(data => this.availableSoftware = data);

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.roomId = +id;
      this.loadRoomData(this.roomId);
    }
  }

  loadRoomData(id: number) {
    this.roomsService.getRoom(id).subscribe({
      next: (data) => {
        this.room.name = data.name;
        this.room.capacity = data.capacity;
        this.room.camp = data.camp;
        this.room.place = data.place;
        
        // Load associated software IDs
        if (data.software) {
            this.room.softwareIds = data.software.map(s => s.id);
        }
      },
      error: (err) => console.error('Error loading classroom', err)
    });
  }

  save() {
    console.log("Sending classroom:", this.room);
    if (this.isEditMode && this.roomId) {
      this.roomsService.updateRoom(this.roomId, this.room).subscribe({
        next: (response) => {
            alert('Classroom edited correctly! ID: ' + response.id);
            this.router.navigate(['/admin/rooms']);
          },
          error: (e) => alert('Update error: ' + e)
        });
      }else{
        this.roomsService.createRoom(this.room).subscribe({
          next: (response) => {
            alert('Classroom created correctly! ID: ' + response.id);
            this.router.navigate(['/admin/rooms']);
          },
          error: (err) => {
            console.error('Error creating classroom:', err);
            alert('Error creating classroom. Check the console for details.');
          }
        });
      }
  }
}
