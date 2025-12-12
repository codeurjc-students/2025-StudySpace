import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';

//Chart.js
Chart.register(...registerables);

@Component({
  selector: 'app-occupancy-stats',
  templateUrl: './occupancy-stats.component.html',
  styleUrls: ['./occupancy-stats.component.css']
})
export class OccupancyStatsComponent implements OnInit, AfterViewInit {

  //reference to HTML
  @ViewChild('occupancyCanvas') occupancyCanvas!: ElementRef;
  @ViewChild('hourlyCanvas') hourlyCanvas!: ElementRef;
  @ViewChild('softwareCanvas') softwareCanvas!: ElementRef;

  private statsUrl = '/api/stats'; 

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    
  }

  ngAfterViewInit(): void {//load data
    this.loadStatsAndCreateCharts();
  }

  loadStatsAndCreateCharts() {
    this.http.get<any>(this.statsUrl).subscribe({
      next: (data) => {
        this.createOccupancyChart(data.occupiedPercentage, data.freePercentage);
        this.createSoftwareChart(data.roomsWithSoftwarePercentage, data.roomsWithoutSoftwarePercentage);
        this.createHourlyChart(data.hourlyOccupancy);
      },
      error: (err) => console.error('Error loading statistics', err)
    });
  }

  //Occupancy Chart
  createOccupancyChart(occupied: number, free: number) {
    new Chart(this.occupancyCanvas.nativeElement, {
      type: 'pie',
      data: {
        labels: ['Occupied', 'Free'],
        datasets: [{
          data: [occupied, free],
          backgroundColor: ['#dc3545', '#198754'], 
          hoverOffset: 4
        }]
      },
      options: { responsive: true, maintainAspectRatio: false }
    });
  }

  // Software Chart
  createSoftwareChart(withSoft: number, withoutSoft: number) {
    new Chart(this.softwareCanvas.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['with Software', 'without Software'],
        datasets: [{
          data: [withSoft, withoutSoft],
          backgroundColor: ['#0d6efd', '#ffc107'], 
          hoverOffset: 4
        }]
      },
      options: { responsive: true, maintainAspectRatio: false }
    });
  }

  //Bar Chart (Hours)
  createHourlyChart(hourlyMap: any) {
    // Convert the Map (e.g., {9: 2, 10: 5}) to arrays for Chart.js
    // Let's create an array of the hours of the working day 
    const hoursLabels = Array.from({length: 14}, (_, i) => i + 8); 
    
    // if no data 0
    const dataValues = hoursLabels.map(h => hourlyMap[h] || 0);

    new Chart(this.hourlyCanvas.nativeElement, {
      type: 'bar',
      data: {
        labels: hoursLabels.map(h => h + ':00'),
        datasets: [{
          label: 'Active Bookings',
          data: dataValues,
          backgroundColor: '#6610f2', 
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: { beginAtZero: true, ticks: { stepSize: 1 } } 
        }
      }
    });
  }
}