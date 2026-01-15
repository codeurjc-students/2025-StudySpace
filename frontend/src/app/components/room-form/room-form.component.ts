import { Component,OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { RoomsService } from '../../services/rooms.service';
import { SoftwareService, SoftwareDTO } from '../../services/software.service';
import { Page } from '../../dtos/page.model'; 

@Component({
  selector: 'app-room-form',
  templateUrl: './room-form.component.html',
  styleUrl: './room-form.component.css'
})
export class RoomFormComponent implements OnInit {

  isEditMode: boolean = false; 
  roomId: number | null = null;
  selectedFile: File | null = null;
  currentImageUrl: string | null = null;

  room = {   //defect values
    name: '',
    capacity: 0,
    camp: 'MOSTOLES', 
    place: '',
    coordenades: '' ,
    active: true,
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
    this.softwareService.getAllSoftwares(0, 100).subscribe({//pool of 100 softwares, check if beteter solution, just for the moment the solution
        next: (data) => {
            this.availableSoftware = data.content;
        },
        error: (err) => console.error('Error loading software:', err)
    });
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
        this.room.coordenades = data.coordenades;
        
        this.room.active = (data.active !== undefined) ? data.active : true; //if undefined we asume true

        // Load associated software IDs
        if (data.software) {
            this.room.softwareIds = data.software.map(s => s.id);
        }
        if (data.imageName) {
            this.currentImageUrl = `https://localhost:8443/api/rooms/${data.id}/image`;
        }
      },
      error: (err) => console.error('Error loading classroom', err)
    });
  }

  save() {
    if (this.isEditMode && this.roomId) {
      this.roomsService.updateRoom(this.roomId, this.room).subscribe({
        next: (response) => {
          if (this.selectedFile) {
              this.uploadImageAndNavigate(this.roomId!);
          } else {
              alert('Classroom updated correctly!');
              this.router.navigate(['/admin/rooms']);
          }
        },
        error: (e) =>{
          if (e.status === 409) {
            alert('Error: A classroom with that name already exists. Please choose another.');
          } else {
            alert('Update error: ' + (e.error?.message || 'Unknown error'));
          }
        }
      });
    } else {
      this.roomsService.createRoom(this.room).subscribe({
        next: (response) => {
          if (this.selectedFile) {
              this.uploadImageAndNavigate(response.id);
          } else {
              alert('Classroom created correctly!');
              this.router.navigate(['/admin/rooms']);
          }
        },
        error: (err) => {
          if (err.status === 409) {
            alert('Error: A classroom with that name already exists. Please choose another.');
          } else {
            alert('Update error: ' + (err.error?.message || 'Unknown error'));
          }
        }
      });
    }
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  

  uploadImageAndNavigate(id: number) {
     this.roomsService.uploadRoomImage(id, this.selectedFile!).subscribe({
         next: () => {
             alert('Room and image saved correctly!');
             this.router.navigate(['/admin/rooms']);
         },
         error: () => {
             alert('Room saved but image upload failed.');
             this.router.navigate(['/admin/rooms']);
         }
     });
  }
}