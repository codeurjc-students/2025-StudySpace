import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RoomsService } from '../../services/rooms.service';
import { RoomDTO } from '../../dtos/room.dto';
import { Chart, registerables } from 'chart.js';

import { CalendarOptions, EventInput } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import bootstrap5Plugin from '@fullcalendar/bootstrap5';

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

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin, bootstrap5Plugin],
    themeSystem: 'bootstrap5',
    initialView: 'dayGridMonth',
    
    //calendar menu
    buttonText: {
      prev: '❮',   
      next: '❯',   
      today: 'Today',
      month: 'MONTH',
      week: 'WEEK'
    },
    // --------------------------------

    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek'
    },
    
    hiddenDays: [0, 6], 
    slotDuration: '00:30:00',
    slotLabelInterval: '01:00',
    slotMinTime: '08:00:00',
    slotMaxTime: '21:00:00',
    allDaySlot: false,
    height: 'auto',
    locale: 'es',
    dayMaxEvents: true,
    displayEventTime: true,
    
    datesSet: (arg) => this.handleDatesSet(arg),
    dateClick: (arg) => this.handleDateClick(arg),
    events: []
  };

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

  





  handleDatesSet(arg: any) {
    // visible dates
    this.loadCalendarData(arg.startStr, arg.endStr);
  }

  // when click on calendar
  handleDateClick(arg: any) {
    this.selectedDate = arg.dateStr; // new date
    this.loadStats(); // load charts
    
    // soft scroll to stats
    document.querySelector('#stats-section')?.scrollIntoView({ behavior: 'smooth' });
  }

  //load data from backend
  loadCalendarData(start: string, end: string) {
    if (!this.roomId) return;

    this.roomsService.getRoomCalendar(this.roomId, start, end).subscribe({
      next: (data) => {
        const processedEvents: any[] = [];

        // reservations/events
        if (data.events) {
          data.events.forEach((evt) => {
            processedEvents.push({
              id: evt.id.toString(),
              title: evt.title || 'Reservado',
              start: evt.start,
              end: evt.end,
              color: '#0d6efd', 
              textColor: 'white',
              display: 'block'
            });
          });
        }

        // 2. Semáforo (Colores Pastel) - AQUI ESTÁ LA CLAVE
        if (data.dailyOccupancy) {
          data.dailyOccupancy.forEach((day) => {
            
            let pastelColor = '#ffffff'; // Blanco por defecto

            // RED
            if (day.status === 'High' || day.color === '#dc3545') {
                pastelColor = '#fadbd8'; // soft red
            } 
            // YELLOW
            else if (day.status === 'Medium' || day.color === '#ffc107') {
                pastelColor = '#fff3cd'; // soft yellow
            } 
            // GREEN
            else if (day.status === 'Low' || day.color === '#198754') {
                pastelColor = '#d1e7dd'; // soft green
            }

            processedEvents.push({
              start: day.date,
              display: 'background',
              backgroundColor: pastelColor, 
              allDay: true 
            });
          });
        }

        //update calendar
        this.calendarOptions = { ...this.calendarOptions, events: processedEvents };
      },
      error: (err) => console.error('Error cargando calendario:', err)
    });
  }

  // --- STATS ---

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
    const hours = Object.keys(stats.hourlyStatus).map(h => h + ':00');
    const dataValues = Object.values(stats.hourlyStatus).map((isOccupied) => isOccupied ? 1 : 0);
    const bgColors = Object.values(stats.hourlyStatus).map((isOccupied) => isOccupied ? '#dc3545' : '#198754');

    if (this.hourlyCanvas) {
      this.hourlyChart = new Chart(this.hourlyCanvas.nativeElement, {
        type: 'bar',
        data: {
          labels: hours,
          datasets: [{
            label: 'Estado (1=Ocupado, 0=Libre)',
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
              max: 1, 
              ticks: { stepSize: 1, callback: (val) => (val === 1 ? 'Ocupado' : 'Libre') } 
            }
          },
          plugins: { legend: { display: false } }
        }
      });
    }

    //global occupation
    if (this.occupancyCanvas) {
      const occupied = stats.occupiedPercentage;
      const free = stats.freePercentage;
      
      this.occupancyChart = new Chart(this.occupancyCanvas.nativeElement, {
        type: 'doughnut',
        data: {
          labels: ['Ocupado (%)', 'Libre (%)'],
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