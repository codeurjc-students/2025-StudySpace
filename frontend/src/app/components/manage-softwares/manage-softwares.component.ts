import { Component, OnInit } from '@angular/core';
import { SoftwareService, SoftwareDTO } from '../../services/software.service';
import { Page } from '../../dtos/page.model';

@Component({
  selector: 'app-manage-softwares',
  templateUrl: './manage-softwares.component.html',
  styleUrls: ['./manage-softwares.component.css']
})
export class ManageSoftwaresComponent implements OnInit {
  softwares: SoftwareDTO[] = [];
  pageData?: Page<SoftwareDTO>;
  currentPage: number = 0;

  constructor(private readonly softwareService: SoftwareService) { }

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number) {
    this.softwareService.getAllSoftwares(page).subscribe({
        next: (data) => {
            this.pageData = data;
            this.softwares = data.content; 
            this.currentPage = data.number;
        },
        error: (e) => console.error(e)
    });
  }
  getPagesArray(): number[] {//helper to create an array with the number of pages, maybe not necessaryyyyyyyyyyyyyyyyyyyyyyyyy
    return Array.from({ length: this.pageData?.totalPages || 0 }, (_, i) => i);
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