import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { LoginService } from './login.service';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';


describe('LoginComponent UI Test', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockLoginService: any;

  beforeEach(async () => {
    //mock the LoginService
    mockLoginService = {
      isLogged: () => false, 
      logIn: jasmine.createSpy('logIn').and.returnValue(of({})) 
    };

    await TestBed.configureTestingModule({
      declarations: [ LoginComponent ],
      imports: [ 
        FormsModule,        // for [(ngModel)]
        RouterTestingModule, // for routerLink
        NgbModule 
      ],
      providers: [
        { provide: LoginService, useValue: mockLoginService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // starts the ngOnInit
  });

  it('Debe mostrar el formulario de login cuando no está logueado', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h3')?.textContent).toContain('Please Log In');
  });

  it('Debe llamar a logIn() cuando el usuario pulsa el botón', () => {
    const usernameInput = fixture.debugElement.query(By.css('input[name="username"]')).nativeElement;
    const passwordInput = fixture.debugElement.query(By.css('input[name="password"]')).nativeElement;
    const loginButton = fixture.debugElement.query(By.css('button[type="submit"]')).nativeElement;

    //fill the form like we are the user
    usernameInput.value = 'admin@test.com';
    usernameInput.dispatchEvent(new Event('input')); //to call angular change detection

    passwordInput.value = '1234';
    passwordInput.dispatchEvent(new Event('input'));

    fixture.detectChanges(); 

    
    loginButton.click();

    //verify
    expect(mockLoginService.logIn).toHaveBeenCalledWith('admin@test.com', '1234');
  });
});



