import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PaginationUtil } from '../../utils/pagination.util'; 

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html'
})
export class PaginationComponent {
  @Input() pageData: any; 
  @Input() currentPage: number = 0;
  @Output() pageChange = new EventEmitter<number>();

  
  get visiblePages(): number[] {
    return PaginationUtil.getVisiblePages(this.pageData, this.currentPage);
  }

  onPageChange(page: number): void {
    if (this.pageData && page >= 0 && page < this.pageData.totalPages) {
      this.pageChange.emit(page);
    }
  }
}