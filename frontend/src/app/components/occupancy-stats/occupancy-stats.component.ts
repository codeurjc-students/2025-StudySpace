import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-occupancy-stats',
  templateUrl: './occupancy-stats.component.html',
  styleUrls: ['./occupancy-stats.component.css']
})
export class OccupancyStatsComponent implements OnInit, AfterViewInit {

  @ViewChild('occupancyCanvas') occupancyCanvas!: ElementRef;
  @ViewChild('hourlyCanvas') hourlyCanvas!: ElementRef;
  @ViewChild('softwareCanvas') softwareCanvas!: ElementRef;

  private occupancyChartInstance: Chart | undefined;
  private softwareChartInstance: Chart | undefined;
  private hourlyChartInstance: Chart | undefined;

  selectedDate: string = '';
  private statsUrl = '/api/stats'; 

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    //YYYY-MM-DD
    const today = new Date();
    const year = today.getFullYear();
    const month = ('0' + (today.getMonth() + 1)).slice(-2);
    const day = ('0' + today.getDate()).slice(-2);
    this.selectedDate = `${year}-${month}-${day}`;
  }

  ngAfterViewInit(): void {
    //to make sure html rendered
    setTimeout(() => {
      this.loadStatsAndCreateCharts();
    }, 100);
  }

  onDateChange(): void {
    this.loadStatsAndCreateCharts();
  }

  loadStatsAndCreateCharts() {
    if (!this.selectedDate) return; 

    const urlWithParams = `${this.statsUrl}?date=${this.selectedDate}`;

    this.http.get<any>(urlWithParams).subscribe({
      next: (data) => {
        this.destroyCharts(); //to clean before painting

        this.createOccupancyChart(data.occupiedPercentage, data.freePercentage);
        
        
        this.createSoftwareChart(data.roomsWithSoftwarePercentage, data.roomsWithoutSoftwarePercentage);
        
        this.createHourlyChart(data.hourlyOccupancy);
      },
      error: (err) => console.error('Error loading stats', err)
    });
  }

  destroyCharts() {
    if (this.occupancyChartInstance) this.occupancyChartInstance.destroy();
    if (this.softwareChartInstance) this.softwareChartInstance.destroy();
    if (this.hourlyChartInstance) this.hourlyChartInstance.destroy();
  }

  createOccupancyChart(occupied: number, free: number) {
    //empty, green
    const hasData = occupied > 0 || free > 0;
    const dataValues = hasData ? [occupied, free] : [0, 1];
    const bgColors = hasData ? ['#dc3545', '#198754'] : ['#e9ecef', '#e9ecef'];
    
    this.occupancyChartInstance = new Chart(this.occupancyCanvas.nativeElement, {
      type: 'pie',
      data: {
        labels: ['Occupied (%)', 'Free (%)'],
        datasets: [{
          data: dataValues,
          backgroundColor: bgColors, 
          hoverOffset: 4
        }]
      },
      options: { 
        responsive: true, 
        maintainAspectRatio: false,
        plugins: {
            legend: { position: 'bottom' }
        }
      }
    });
  }

  createSoftwareChart(withSoft: number, withoutSoft: number) {
    const hasData = withSoft > 0 || withoutSoft > 0;
    const chartData = hasData ? [withSoft, withoutSoft] : [1]; 
    const colors = hasData ? ['#0d6efd', '#ffc107'] : ['#e9ecef']; //grey, no data
    const labels = hasData ? ['With Software (%)', 'Without Software (%)'] : ['No reservations yet'];

    this.softwareChartInstance = new Chart(this.softwareCanvas.nativeElement, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: chartData,
          backgroundColor: colors,
          hoverOffset: 4
        }]
      },
      options: { 
        responsive: true, 
        maintainAspectRatio: false,
        plugins: {
            legend: { position: 'bottom' },
            tooltip: { enabled: hasData } 
        }
      }
    });
  }

  createHourlyChart(hourlyMap: any) {
    const hoursLabels = Array.from({length: 14}, (_, i) => i + 8); 
    const dataValues = hoursLabels.map(h => hourlyMap[h] || 0);

    this.hourlyChartInstance = new Chart(this.hourlyCanvas.nativeElement, {
      type: 'bar',
      data: {
        labels: hoursLabels.map(h => h + ':00'),
        datasets: [{
          label: 'Bookings Count',
          data: dataValues,
          backgroundColor: '#6610f2', 
          borderRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: { 
            beginAtZero: true, 
            ticks: { stepSize: 1 } 
          } 
        }
      }
    });
  }
}