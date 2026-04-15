import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VerifyEmailComponent } from './verify-email.component';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

describe('VerifyEmailComponent', () => {
  let component: VerifyEmailComponent;
  let fixture: ComponentFixture<VerifyEmailComponent>;
  let httpMock: HttpTestingController;
  let mockActivatedRoute: any;

  beforeEach(async () => {
    mockActivatedRoute = {
      snapshot: {
        queryParamMap: {
          get: jasmine.createSpy('get').and.returnValue('fake-token-123'),
        },
      },
    };

    await TestBed.configureTestingModule({
      declarations: [VerifyEmailComponent],
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [{ provide: ActivatedRoute, useValue: mockActivatedRoute }],
    }).compileComponents();

    fixture = TestBed.createComponent(VerifyEmailComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle missing token on init', () => {
    //No token in URL
    mockActivatedRoute.snapshot.queryParamMap.get.and.returnValue(null);

    component.ngOnInit();

    expect(component.loading).toBeFalse();
    expect(component.success).toBeFalse();
    expect(component.errorMessage).toBe(
      'No verification token provided in the link.',
    );
  });

  it('should call verifyToken and handle SUCCESS response', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne(
      '/api/auth/verify-email?token=fake-token-123',
    );
    expect(req.request.method).toBe('GET');

    req.flush({ message: 'Email verified' });

    expect(component.loading).toBeFalse();
    expect(component.success).toBeTrue();
  });

  it('should handle ERROR response with err.error.message', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne(
      '/api/auth/verify-email?token=fake-token-123',
    );

    req.flush(
      { message: 'Token expired' },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(component.loading).toBeFalse();
    expect(component.success).toBeFalse();
    expect(component.errorMessage).toBe('Token expired');
  });

  it('should handle ERROR response with plain err.error string', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne(
      '/api/auth/verify-email?token=fake-token-123',
    );

    req.flush('Invalid token string', {
      status: 400,
      statusText: 'Bad Request',
    });

    expect(component.loading).toBeFalse();
    expect(component.success).toBeFalse();
    expect(component.errorMessage).toBe('Invalid token string');
  });

  it('should handle ERROR response with default fallback message', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne(
      '/api/auth/verify-email?token=fake-token-123',
    );

    req.flush(null, { status: 500, statusText: 'Server Error' });

    expect(component.loading).toBeFalse();
    expect(component.success).toBeFalse();
    expect(component.errorMessage).toBe(
      'The verification link is invalid or has expired.',
    );
  });
});
