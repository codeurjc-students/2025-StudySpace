import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RoomsService } from '../../services/rooms.service';
import { RoomDTO } from '../../dtos/room.dto';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-room-detail',
  templateUrl: './room-detail.component.html',
  styleUrls: ['./room-detail.component.css']
})
export class RoomDetailComponent implements OnInit {

  room: RoomDTO | undefined;
  roomId!: number;

  selectedDate: string = '';
  @ViewChild('hourlyCanvas') hourlyCanvas!: ElementRef;
  @ViewChild('occupancyCanvas') occupancyCanvas!: ElementRef;
  
  private hourlyChart: Chart | undefined;
  private occupancyChart: Chart | undefined;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly roomsService: RoomsService
  ) {}

  ngOnInit(): void {
    // initialised todays date
    const today = new Date();
    this.selectedDate = today.toISOString().split('T')[0];

    const idParam = this.route.snapshot.params['id'];
    if (idParam) {
      this.roomId = +idParam;
      this.loadRoomDetails();
      setTimeout(() => this.loadStats(), 100); // for letting the canva load correctly
    }
  }

  loadRoomDetails() {
    this.roomsService.getRoom(this.roomId).subscribe({
      next: (data) => this.room = data,
      error: (err) => console.error(err)
    });
  }

  onDateChange() {
    this.loadStats();
  }

  loadStats() {
    if (!this.roomId) return;

    this.roomsService.getRoomStats(this.roomId, this.selectedDate).subscribe({
      next: (stats) => {
        this.destroyCharts();
        this.createCharts(stats);
      },
      error: (err) => console.error('Error loading room stats', err)
    });
  }

  destroyCharts() {
    if (this.hourlyChart) this.hourlyChart.destroy();
    if (this.occupancyChart) this.occupancyChart.destroy();
  }

  createCharts(stats: any) {
    // 1. Gráfico Horario (Barras)
    const hours = Object.keys(stats.hourlyStatus).map(h => h + ':00');
    // transform 1 occupied to 0 free
    const dataValues = Object.values(stats.hourlyStatus).map((isOccupied) => isOccupied ? 1 : 0);
    //red occupied green free
    const bgColors = Object.values(stats.hourlyStatus).map((isOccupied) => isOccupied ? '#dc3545' : '#198754');

    if (this.hourlyCanvas) {
      this.hourlyChart = new Chart(this.hourlyCanvas.nativeElement, {
        type: 'bar',
        data: {
          labels: hours,
          datasets: [{
            label: 'Occupancy (1=Busy, 0=Free)',
            data: dataValues,
            backgroundColor: bgColors,
            borderRadius: 4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: { 
              beginAtZero: true, 
              max: 1, //max 1 reservation per room
              ticks: { 
                stepSize: 1,
                callback: (val) => (val === 1 ? 'Occupied' : 'Free') 
              } 
            }
          },
          plugins: { legend: { display: false } }
        }
      });
    }

    //global ocupancy
    if (this.occupancyCanvas) {
      const occupied = stats.occupiedPercentage;
      const free = stats.freePercentage;
      
      this.occupancyChart = new Chart(this.occupancyCanvas.nativeElement, {
        type: 'doughnut',
        data: {
          labels: ['Occupied (%)', 'Free (%)'],
          datasets: [{
            data: [occupied, free],
            backgroundColor: ['#dc3545', '#198754'],
            hoverOffset: 4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { position: 'bottom' } }
        }
      });
    }
  }
}