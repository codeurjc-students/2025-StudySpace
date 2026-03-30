import { Injectable } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { SharedModalComponent } from '../components/shared-modal/shared-modal.component';

@Injectable({
  providedIn: 'root',
})
export class DialogService {
  constructor(private modalService: NgbModal) {}

  //alert()
  alert(title: string, message: string): Promise<any> {
    const modalRef = this.modalService.open(SharedModalComponent, {
      backdrop: 'static',
    });
    modalRef.componentInstance.title = title;
    modalRef.componentInstance.message = message;
    modalRef.componentInstance.type = 'alert';
    return modalRef.result.catch(() => false);
  }

  //confirm()
  confirm(title: string, message: string): Promise<boolean> {
    const modalRef = this.modalService.open(SharedModalComponent, {
      backdrop: 'static',
    });
    modalRef.componentInstance.title = title;
    modalRef.componentInstance.message = message;
    modalRef.componentInstance.type = 'confirm';
    return modalRef.result.catch(() => false);
  }

  //prompt()
  prompt(title: string, message: string): Promise<string | null> {
    const modalRef = this.modalService.open(SharedModalComponent, {
      backdrop: 'static',
    });
    modalRef.componentInstance.title = title;
    modalRef.componentInstance.message = message;
    modalRef.componentInstance.type = 'prompt';
    return modalRef.result.catch(() => null);
  }
}
