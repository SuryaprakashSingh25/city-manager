import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResetPasswordComponent } from './reset-password.component';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { NgForm } from '@angular/forms';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let authServiceMock: any;
  let toastServiceMock: any;
  let router: Router;

  beforeEach(async () => {
    authServiceMock = {
      resetPassword: jasmine.createSpy('resetPassword')
    };

    toastServiceMock = {
      success: jasmine.createSpy('success'),
      error: jasmine.createSpy('error')
    };

    await TestBed.configureTestingModule({
      imports: [ResetPasswordComponent, RouterTestingModule],
      providers: [
        { 
          provide: AuthService, 
          useValue: authServiceMock 
        },
        { 
          provide: ToastService, 
          useValue: toastServiceMock 
        },
        { 
          provide: ActivatedRoute, 
          useValue: { snapshot: { queryParamMap: { get: () => 'dummy-token' } } } 
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(component.token).toBe('dummy-token');
  });

  it('should show error if form is invalid', () => {
    const form = { invalid: true } as NgForm;
    component.submit(form);
    expect(toastServiceMock.error).toHaveBeenCalledWith('Please fill in all required fields correctly.');
  });

  it('should show error if passwords do not match', () => {
    component.newPassword = '123';
    component.confirmPassword = '456';
    const form = { invalid: false } as NgForm;
    component.submit(form);
    expect(toastServiceMock.error).toHaveBeenCalledWith('Passwords do not match.');
  });

  it('should call authService.resetPassword and navigate on success', () => {
    component.newPassword = 'password123';
    component.confirmPassword = 'password123';
    const form = { invalid: false } as NgForm;

    authServiceMock.resetPassword.and.returnValue(of({ message: 'Password reset successfully.' }));

    component.submit(form);

    expect(authServiceMock.resetPassword).toHaveBeenCalledWith({
      token: 'dummy-token',
      newPassword: 'password123'
    });
    expect(toastServiceMock.success).toHaveBeenCalledWith('Password reset successfully.');
  });

  it('should show error if resetPassword fails', () => {
    component.newPassword = 'password123';
    component.confirmPassword = 'password123';
    const form = { invalid: false } as NgForm;

    const errorResponse = { error: { message: 'Token expired' } };
    authServiceMock.resetPassword.and.returnValue(throwError(() => errorResponse));

    component.submit(form);

    expect(toastServiceMock.error).toHaveBeenCalledWith('Token expired');
  });

  it('should toggle showPassword flags correctly', () => {
    expect(component.showNewPassword).toBeFalse();
    expect(component.showConfirmPassword).toBeFalse();
    component.showNewPassword = !component.showNewPassword;
    component.showConfirmPassword = !component.showConfirmPassword;
    expect(component.showNewPassword).toBeTrue();
    expect(component.showConfirmPassword).toBeTrue();
  });
});
