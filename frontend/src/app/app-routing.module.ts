import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { RoomDetailComponent } from './components/room-detail/room-detail.component';
import { RegisterComponent } from './login/register/register.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { authGuard } from './auth.guard';
import { ReservationFormComponent } from './components/reservation-form/reservation-form.component';


const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  { path: 'register', component: RegisterComponent },
  { path: 'rooms/:id', component: RoomDetailComponent },
  { path: 'profile', component: UserProfileComponent },
  { 
    path: 'profile', 
    component: UserProfileComponent, 
    canActivate: [authGuard] // only authenticated users can access
  },
  { 
    path: 'reservations/create', 
    component: ReservationFormComponent,
    canActivate: [authGuard] // only authenticated users can access
  },
  {path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }



