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
    this.loadSoftwares(0);
  }

  loadSoftwares(page: number) {
    this.softwareService.getAllSoftwares(page).subscribe({
        next: (data) => {
            this.pageData = data;
            this.softwares = data.content; 
            this.currentPage = data.number;
        },
        error: (e) => console.error(e)
    });
  }
  getVisiblePages(): number[] {
    if (!this.pageData) return [];

    const totalPages = this.pageData.totalPages;
    const maxPagesToShow = 10; 


    if (totalPages <= maxPagesToShow) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    let startPage = this.currentPage - Math.floor(maxPagesToShow / 2);
    let endPage = this.currentPage + Math.ceil(maxPagesToShow / 2);

    if (startPage < 0) {
      startPage = 0;
      endPage = maxPagesToShow;
    }

    if (endPage > totalPages) {
      endPage = totalPages;
      startPage = totalPages - maxPagesToShow;
    }

    const pages = [];
    for (let i = startPage; i < endPage; i++) {
      pages.push(i);
    }
    return pages;
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