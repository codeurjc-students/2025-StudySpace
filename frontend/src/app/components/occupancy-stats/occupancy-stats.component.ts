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
      if (this.hourlyChartInstance) {
      this.hourlyChartInstance.destroy();
    }

    const labels: string[] = [];
    const dataValues: number[] = [];
    
    for (let i = 8; i < 21; i += 0.5) {
      const hour = Math.floor(i);
      const minutes = (i % 1 === 0) ? '00' : '30';
      const timeLabel = `${this.pad(hour)}:${minutes}`;
      
      labels.push(timeLabel);


      const val = hourlyMap[i] || hourlyMap[timeLabel] || 0;
      dataValues.push(val);
    }


    const ctx = this.hourlyCanvas.nativeElement.getContext('2d');
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(102, 16, 242, 0.8)'); 
    gradient.addColorStop(1, 'rgba(102, 16, 242, 0.1)'); 

    this.hourlyChartInstance = new Chart(this.hourlyCanvas.nativeElement, {
      type: 'bar', 
      data: {
        labels: labels,
        datasets: [{
          label: 'Active Reservations',
          data: dataValues,
          backgroundColor: gradient,
          borderColor: '#6610f2',
          borderWidth: 1,
          borderRadius: 5, 
          barPercentage: 0.7, 
          categoryPercentage: 0.9
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }, 
          tooltip: {
            callbacks: {
              label: (context) => ` ${context.parsed.y} Reservations`
            },
            backgroundColor: 'rgba(0,0,0,0.8)',
            padding: 10,
            cornerRadius: 8,
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1, 
              font: { family: "'Segoe UI', sans-serif", size: 11 }
            },
            grid: {
              color: '#f0f0f0' 
            },
            title: { display: true, text: 'Occupancy Count', font: {size: 12, weight: 'bold'} }
          },
          x: {
            grid: { display: false }, 
            ticks: {
              autoSkip: false, 
              maxRotation: 45, 
              minRotation: 45,
              font: { size: 10 }
            }
          }
        }
      }
    });
  }


  private pad(n: number): string {
    return n < 10 ? '0' + n : '' + n;
  }
}