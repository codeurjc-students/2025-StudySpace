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
  loginData = { user: '', password: '' };
  //public passwordVisible: boolean = false;

  //put the loginService in the constructor, router and modalService too
  constructor(
    public loginService: LoginService, 
    private readonly modalService: NgbModal,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  

  logIn() {
    this.loginService.logIn(this.loginData.user, this.loginData.password).subscribe({
      next: (response: any) => {
        console.log("Correct login:", response);
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


      error: (err: any) => {
        console.error("Login error:", err);
      
        if (err.status === 401) {
             
            const errorMsg = err.error?.message || err.error?.error || "";

            
            if (errorMsg.toLowerCase().includes("locked")) {
                 alert("⛔ ACCESS DENIED\n\n Your account has been LOCKED by an administrator.\n Contact support.");
                 this.loginService.logOut(); 
                 this.router.navigate(['/']);
            } else {
              this.modalService.open(this.loginErrorModal);   
            }
            
        } else {
             this.modalService.open(this.loginErrorModal);
        }
      }
    });
  }

  logOut() {
    this.loginService.logOut();
  }
}
