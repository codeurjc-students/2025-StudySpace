import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-room-search-form',
  templateUrl: './room-search-form.component.html',
})
export class RoomSearchFormComponent {
  @Input() searchText: string = '';
  @Output() searchTextChange = new EventEmitter<string>();

  @Input() campus: string = '';
  @Output() campusChange = new EventEmitter<string>();

  @Input() capacity: number | null = null;
  @Output() capacityChange = new EventEmitter<number | null>();

  @Input() filterActive: string = '';
  @Output() filterActiveChange = new EventEmitter<string>();

  @Input() showActiveFilter: boolean = false;
  @Input() isSearching: boolean = false;

  @Output() search = new EventEmitter<void>();
  @Output() clear = new EventEmitter<void>();

  triggerSearch() {
    this.search.emit();
  }

  triggerClear() {
    this.clear.emit();
  }

  onTextChange(val: string) {
    this.searchText = val;
    this.searchTextChange.emit(val);
  }
  onCampusChange(val: string) {
    this.campus = val;
    this.campusChange.emit(val);
  }
  onCapacityChange(val: number | null) {
    this.capacity = val;
    this.capacityChange.emit(val);
  }
  onActiveChange(val: string) {
    this.filterActive = val;
    this.filterActiveChange.emit(val);
  }
}
