import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AppComponent } from './app.component';
import { LoginService } from './login/login.service';
import { Router } from '@angular/router';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let router: Router;
  let loginService: LoginService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientTestingModule 
      ],
      declarations: [
        AppComponent
      ],
      providers: [
        LoginService 
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router); 
    loginService = TestBed.inject(LoginService);
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it(`should have as title 'frontend'`, () => {
    expect(component.title).toEqual('frontend');
  });


  
  it('goToLogIn should log message and navigate to /login', () => {
    const navigateSpy = spyOn(router, 'navigate');
    
    spyOn(console, 'log');

    component.goToLogIn();

    expect(console.log).toHaveBeenCalledWith('Navegando a login...');
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('getUserImageUrl should return placeholder if no user is logged in', () => {
    loginService.currentUser = null;
    expect(component.getUserImageUrl()).toBe('assets/user_placeholder.png');
  });

  it('getUserImageUrl should return placeholder if user exists but has no imageName', () => {
    loginService.currentUser = { id: 1, name: 'Test', imageName: undefined } as any;
    expect(component.getUserImageUrl()).toBe('assets/user_placeholder.png');
  });

  it('getUserImageUrl should return API URL if user has imageName', () => {
    loginService.currentUser = { id: 99, name: 'Test', imageName: 'photo.jpg' } as any;
    const url = component.getUserImageUrl();
    
    expect(url).toContain('https://localhost:8443/api/users/99/image');
  });
});