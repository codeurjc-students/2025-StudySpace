import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';//prueba 1001

@Component({
  selector: 'app-root',
  standalone: true,//prueba 1001
  imports: [RouterOutlet, RouterLink],//prueba 1001
  templateUrl: './app.component.html',
  styleUrls: ['./app.scss']
})
export class AppComponent {
  title = 'Frontend';
    //constructor(private router: Router) {}
  goToPage() :void{
    console.log("Navigating to page...");
    //this.router.navigate(['/home']);
  }
}


   




