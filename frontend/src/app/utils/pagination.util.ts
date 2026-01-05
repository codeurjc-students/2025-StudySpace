import { Page } from '../dtos/page.model';

export class PaginationUtil {
  
  
  static getVisiblePages(pageData: Page<any> | undefined | null, currentPage: number, maxPagesToShow: number = 10): number[] {
    if (!pageData) return [];

    const totalPages = pageData.totalPages;

    if (totalPages <= maxPagesToShow) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    let startPage = currentPage - Math.floor(maxPagesToShow / 2);
    let endPage = currentPage + Math.ceil(maxPagesToShow / 2);

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
}