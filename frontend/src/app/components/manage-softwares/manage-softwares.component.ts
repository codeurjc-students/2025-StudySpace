import { Component, OnInit } from '@angular/core';
import { SoftwareService, SoftwareDTO } from '../../services/software.service';
import { Page } from '../../dtos/page.model';
import { PaginationUtil } from '../../utils/pagination.util';

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
    return PaginationUtil.getVisiblePages(this.pageData, this.currentPage);
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