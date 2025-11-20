import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

// 1. Import HttpClientModule for REST
// (Ref: Unit 3, page 12-13)
import { HttpClientModule } from '@angular/common/http';
// 2. Import FormsModule to use [(ngModel)] in the login
// (Ref: Unit 2, page 10)
import { FormsModule } from '@angular/forms'; 

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { RoomDetailComponent } from './components/room-detail/room-detail.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomeComponent,
    RoomDetailComponent
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
