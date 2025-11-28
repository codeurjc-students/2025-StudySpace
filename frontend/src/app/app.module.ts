import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
// 1. Import HttpClientModule for REST
import { HttpClientModule } from '@angular/common/http';
// 2. Import FormsModule to use [(ngModel)] in the login
import { FormsModule } from '@angular/forms'; 
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { RoomDetailComponent } from './components/room-detail/room-detail.component';
import { RegisterComponent } from './login/register/register.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { ReservationFormComponent } from './components/reservation-form/reservation-form.component';
import { AdminMenuComponent } from './components/admin-menu/admin-menu.component';
import { ManageRoomsComponent } from './components/manage-rooms/manage-rooms.component';
import { ManageUsersComponent } from './components/manage-users/manage-users.component';
import { OccupancyStatsComponent } from './components/occupancy-stats/occupancy-stats.component';
import { RoomFormComponent } from './components/room-form/room-form.component';
import { ManageSoftwaresComponent } from './components/manage-softwares/manage-softwares.component';
import { SoftwareFormComponent } from './components/software-form/software-form.component';
import { ManageReservationsComponent } from './components/manage-reservations/manage-reservations.component';



@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomeComponent,
    RoomDetailComponent,
    RegisterComponent,
    UserProfileComponent,
    ReservationFormComponent,
    AdminMenuComponent,
    ManageRoomsComponent,
    ManageUsersComponent,
    OccupancyStatsComponent,
    RoomFormComponent,
    ManageSoftwaresComponent,
    SoftwareFormComponent,
    ManageReservationsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,

    // 3. Add HttpClientModule to imports array
    HttpClientModule,

    // 4. Add FormsModule to imports array
    FormsModule,
      NgbModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
