import { Component, ViewChild, TemplateRef } from '@angular/core';
import { LoginService } from './login.service'; 
import { NgbModal } from '@ng-bootstrap/ng-bootstrap'; 
import { Router, ActivatedRoute  } from '@angular/router';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
@ViewChild('loginErrorModal') loginErrorModal!: TemplateRef<any>;

  //put the loginService in the constructor, router and modalService too
  constructor(
    public loginService: LoginService, 
    private modalService: NgbModal,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  logIn(user: string, pass: string) {
    this.loginService.logIn(user, pass).subscribe({
      next: (response: any) => {
        console.log("Login correcto:", response);
        // Optional: Redirect to home or another page after successful login
        // this.router.navigate(['/home']);
        const returnUrl = this.route.snapshot.queryParams['returnUrl'];
        if (returnUrl) {
            this.router.navigateByUrl(returnUrl);
        } else {
            //Instead of going to '/profile', let's go to '/reservations/create'
            //this.router.navigate(['/reservations/create']); 
            this.router.navigate(['/']); 
        }
      },
      error: (error: any) => {
        console.error("Login error:", error);
      
        this.modalService.open(this.loginErrorModal);
      }
    });
  }

  logOut() {
    this.loginService.logOut();
  }
}
