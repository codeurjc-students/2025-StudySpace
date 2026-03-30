import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-shared-modal',
  templateUrl: './shared-modal.component.html',
})
export class SharedModalComponent {
  @Input() title: string = 'Notice';
  @Input() message: string = '';
  @Input() type: 'alert' | 'confirm' | 'prompt' = 'alert';
  @Input() promptValue: string = '';

  constructor(public activeModal: NgbActiveModal) {}

  confirm() {
    if (this.type === 'prompt') {
      this.activeModal.close(this.promptValue);
    } else {
      this.activeModal.close(true);
    }
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
