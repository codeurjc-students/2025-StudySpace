import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { RoomDetailComponent } from './components/room-detail/room-detail.component';
import { RegisterComponent } from './login/register/register.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { authGuard } from './security/auth.guard';
import { ReservationFormComponent } from './components/reservation-form/reservation-form.component';
import { AdminMenuComponent } from './components/admin-menu/admin-menu.component';
import { ManageRoomsComponent } from './components/manage-rooms/manage-rooms.component';
import { ManageUsersComponent } from './components/manage-users/manage-users.component';
import { OccupancyStatsComponent } from './components/occupancy-stats/occupancy-stats.component';
import { adminGuard } from './security/admin.guard';
import { RoomFormComponent } from './components/room-form/room-form.component';
import { ManageSoftwaresComponent } from './components/manage-softwares/manage-softwares.component';
import { SoftwareFormComponent } from './components/software-form/software-form.component';
import { ManageReservationsComponent } from './components/manage-reservations/manage-reservations.component';
import { ResetPasswordComponent } from './login/reset-password/reset-password.component';
import { ForgotPasswordComponent } from './login/forgot-password/forgot-password.component';


const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  { path: 'register', component: RegisterComponent },
  { path: 'rooms/:id', component: RoomDetailComponent },
  { path: 'profile', component: UserProfileComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
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
  //admin routes
  { path: 'admin', component: AdminMenuComponent, canActivate: [adminGuard] },
  { path: 'admin/rooms', component: ManageRoomsComponent, canActivate: [adminGuard] },
  { path: 'admin/rooms/create', component: RoomFormComponent, canActivate: [adminGuard] },
  { path: 'admin/rooms/edit/:id', component: RoomFormComponent, canActivate: [adminGuard] },

  { path: 'admin/users', component: ManageUsersComponent, canActivate: [adminGuard] },
  { path: 'admin/users/:userId/reservations', component: ManageReservationsComponent, canActivate: [adminGuard] },
  
  { path: 'admin/softwares', component: ManageSoftwaresComponent, canActivate: [adminGuard] },
  { path: 'admin/softwares/create', component: SoftwareFormComponent, canActivate: [adminGuard] },
  { path: 'admin/softwares/edit/:id', component: SoftwareFormComponent, canActivate: [adminGuard] },

  { path: 'admin/stats', component: OccupancyStatsComponent, canActivate: [adminGuard] },
  {path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }



