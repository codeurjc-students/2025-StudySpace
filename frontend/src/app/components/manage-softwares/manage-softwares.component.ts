import { Component, OnInit } from '@angular/core';
import { SoftwareService, SoftwareDTO } from '../../services/software.service';

@Component({
  selector: 'app-manage-softwares',
  templateUrl: './manage-softwares.component.html',
  styleUrls: ['./manage-softwares.component.css']
})
export class ManageSoftwaresComponent implements OnInit {
  softwares: SoftwareDTO[] = [];

  constructor(private softwareService: SoftwareService) { }

  ngOnInit(): void {
    this.loadSoftwares();
  }

  loadSoftwares() {
    this.softwareService.getAllSoftwares().subscribe({
        next: (data) => this.softwares = data,
        error: (e) => console.error(e)
    });
  }

  deleteSoftware(id: number) {
    if(confirm("Are you sure you want to delete this software?")) {
        this.softwareService.deleteSoftware(id).subscribe({
            next: () => {
                this.softwares = this.softwares.filter(s => s.id !== id);
                alert("Software deleted!");
            },
            error: () => alert("Error deleting software.")
        });
    }
  }
}