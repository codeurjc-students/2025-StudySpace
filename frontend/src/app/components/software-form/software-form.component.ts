import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SoftwareService } from '../../services/software.service';
import { handleSaveRequest } from '../../utils/form-helpers.util';

@Component({
  selector: 'app-software-form',
  templateUrl: './software-form.component.html',
  styleUrls: ['./software-form.component.css']
})
export class SoftwareFormComponent implements OnInit {
  isEditMode = false;
  softwareId: number | null = null;

  software = {
    name: '',
    version: 1.0,
    description: ''
  };

  constructor(
    private readonly softwareService: SoftwareService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.softwareId = +id;
      this.softwareService.getSoftware(+id).subscribe(data => {
          this.software = { 
              name: data.name, 
              version: Number(data.version), 
              description: data.description 
          };
      });
    }
  }

  save() {
      const request$ = (this.isEditMode && this.softwareId)
          ? this.softwareService.updateSoftware(this.softwareId, this.software)
          : this.softwareService.createSoftware(this.software);

      handleSaveRequest(
          request$,
          () => {
              this.router.navigate(['/admin/softwares']);
          },
          'Software', 
          'Error: This software version already exists. Please change the name or version.' // 409
      );
  }
}