import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VerifyReservationComponent } from './verify-reservation.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

describe('VerifyReservationComponent', () => {
  let component: VerifyReservationComponent;
  let fixture: ComponentFixture<VerifyReservationComponent>;
  let httpMock: HttpTestingController;

  const mockActivatedRoute = {
    snapshot: {
      queryParamMap: {
        get: (key: string) => key === 'token' ? 'valid-token-123' : null
      }
    }
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VerifyReservationComponent],
      imports: [
        HttpClientTestingModule, 
        RouterTestingModule      
      ],
      providers: [
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VerifyReservationComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    fixture.detectChanges(); 
    const req = httpMock.expectOne(req => req.url.includes('/api/reservations/verify'));
    expect(component).toBeTruthy();
  });

  it('should verify successfully when token is valid (200 OK)', () => {
    fixture.detectChanges(); // ngOnInit

    const req = httpMock.expectOne(req => req.url === '/api/reservations/verify' && req.params.get('token') === 'valid-token-123');
    expect(req.request.method).toBe('GET');

    req.flush('Verification successful', { status: 200, statusText: 'OK' });

    expect(component.loading).toBeFalse();
    expect(component.success).toBeTrue();
    expect(component.errorMessage).toBe('');
  });

  it('should show error when token is invalid or expired (400 Bad Request)', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne(r => r.url.includes('/verify'));
    //simulaet backend error
    const errorMsg = 'Token has expired';
    req.flush(errorMsg, { status: 400, statusText: 'Bad Request' });

    expect(component.loading).toBeFalse();
    expect(component.success).toBeFalse();
    expect(component.errorMessage).toBe(errorMsg);
  });

  it('should show error immediately if no token is present in URL', () => {
    //overwrite ActivatedRoute just for this test without token
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      declarations: [VerifyReservationComponent],
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        { 
          provide: ActivatedRoute, 
          useValue: { snapshot: { queryParamMap: { get: () => null } } } // Token null
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(VerifyReservationComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);

    fixture.detectChanges();
    httpMock.expectNone('/api/reservations/verify');

    expect(component.loading).toBeFalse();
    expect(component.success).toBeFalse();
    expect(component.errorMessage).toContain('No verification token provided');
  });
});