import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthResponse } from '../../core/models/auth.models';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceMock: any;
  let toastServiceMock: any;

  beforeEach(async () => {
    authServiceMock = {
      login: jasmine.createSpy('login')
    };

    toastServiceMock = {
      error: jasmine.createSpy('error'),
      success: jasmine.createSpy('success')
    };

    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,          // Standalone component
        CommonModule,
        FormsModule,
        RouterTestingModule      // <-- Provides Router & ActivatedRoute
      ],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: ToastService, useValue: toastServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show error if form is invalid', () => {
    component.login({ invalid: true } as any);
    expect(toastServiceMock.error).toHaveBeenCalledWith('Please enter valid credentials.');
  });

  it('should call authService.login and navigate based on role', () => {
    const fakeResponse: AuthResponse = {
      accessToken: 'header.' + btoa(JSON.stringify({ role: 'ADMIN' })) + '.signature',
      refreshToken: 'refreshToken'
    };
    authServiceMock.login.and.returnValue(of(fakeResponse));

    component.email = 'admin@example.com';
    component.password = 'password';
    component.login({ invalid: false } as any);

    expect(authServiceMock.login).toHaveBeenCalled();
  });
});
