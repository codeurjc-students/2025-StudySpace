import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { RoomsService } from '../../services/rooms.service';
import { SoftwareService, SoftwareDTO } from '../../services/software.service';
import { handleSaveRequest } from '../../utils/form-helpers.util';
import { DialogService } from '../../services/dialog.service';

@Component({
  selector: 'app-room-form',
  templateUrl: './room-form.component.html',
  styleUrl: './room-form.component.css',
})
export class RoomFormComponent implements OnInit {
  isEditMode: boolean = false;
  roomId: number | null = null;
  selectedFile: File | null = null;
  currentImageUrl: string | null = null;

  softwareSearchText: string = '';
  softwareMinVersion: number | null = null;
  availableSoftwares: SoftwareDTO[] = [];
  selectedSoftwares: SoftwareDTO[] = [];

  room = {
    //defect values
    name: '',
    capacity: 0,
    camp: 'MOSTOLES',
    place: '',
    coordenades: '',
    active: true,
    softwareIds: [] as number[], // Array to hold selected software IDs
  };

  campusOptions = [
    'ALCORCON',
    'MOSTOLES',
    'VICALVARO',
    'FUENLABRADA',
    'QUINTANA',
  ];
  availableSoftware: SoftwareDTO[] = [];

  constructor(
    private readonly roomsService: RoomsService,
    private readonly softwareService: SoftwareService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly dialogService: DialogService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.roomId = +id;
      this.roomsService.getRoom(+id).subscribe({
        next: (data) => {
          this.room = {
            name: data.name,
            capacity: data.capacity,
            camp: data.camp,
            place: data.place,
            coordenades: data.coordenades,
            active: data.active,
            softwareIds: data.software
              ? data.software.map((s: any) => s.id)
              : [],
          };

          if (data.imageName) {
            this.currentImageUrl = `/api/rooms/${data.id}/image`;
          }

          this.selectedSoftwares = data.software ? [...data.software] : [];
        },
        error: (err) => console.error('Error loading classroom', err),
      });
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

        this.room.active = data.active ?? true; //if undefined we asume true

        // Load associated software IDs
        if (data.software) {
          this.room.softwareIds = data.software.map((s) => s.id);
        }
        if (data.imageName) {
          this.currentImageUrl = `https://localhost:8443/api/rooms/${data.id}/image`;
        }
      },
      error: (err) => console.error('Error loading classroom', err),
    });
  }

  save() {
    const request$ =
      this.isEditMode && this.roomId
        ? this.roomsService.updateRoom(this.roomId, this.room)
        : this.roomsService.createRoom(this.room);

    handleSaveRequest(
      request$,
      (response) => {
        if (this.selectedFile) {
          this.uploadImageAndNavigate(response.id);
        } else {
          const action = this.isEditMode ? 'updated' : 'created';
          this.dialogService.alert(`Classroom ${action} correctly!`, '');
          this.router.navigate(['/admin/rooms']);
        }
      },
      'Classroom',
      'Error: A classroom with that name already exists. Please choose another.', // 409
    );
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  uploadImageAndNavigate(id: number) {
    this.roomsService.uploadRoomImage(id, this.selectedFile!).subscribe({
      next: () => {
        this.dialogService.alert('Success', 'Room and image saved correctly!');
        this.router.navigate(['/admin/rooms']);
      },
      error: () => {
        this.dialogService.alert(
          'Error',
          'Room saved but image upload failed.',
        );
        this.router.navigate(['/admin/rooms']);
      },
    });
  }

  searchSoftwareForDropdown() {
    if (!this.softwareSearchText && !this.softwareMinVersion) {
      this.availableSoftwares = [];
      return;
    }

    this.softwareService
      .searchSoftwares(
        this.softwareSearchText,
        this.softwareMinVersion || undefined,
      )
      .subscribe({
        next: (data) => {
          const selectedIds = new Set(this.selectedSoftwares.map((s) => s.id));
          this.availableSoftwares = data.content.filter(
            (s) => !selectedIds.has(s.id),
          );
        },
        error: (e) => console.error(e),
      });
  }

  addSoftwareToRoom(software: SoftwareDTO) {
    if (!this.room.softwareIds) {
      this.room.softwareIds = [];
    }

    this.room.softwareIds.push(software.id);
    this.selectedSoftwares.push(software);

    this.availableSoftwares = this.availableSoftwares.filter(
      (s) => s.id !== software.id,
    );
  }

  removeSoftwareFromRoom(softwareId: number) {
    this.room.softwareIds = this.room.softwareIds.filter(
      (id) => id !== softwareId,
    );
    this.selectedSoftwares = this.selectedSoftwares.filter(
      (s) => s.id !== softwareId,
    );

    this.searchSoftwareForDropdown();
  }
}
