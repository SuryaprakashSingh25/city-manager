import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ForgotPasswordComponent } from './forgot-password.component';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';
import { of, throwError } from 'rxjs';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

describe('ForgotPasswordComponent', () => {
  let component: ForgotPasswordComponent;
  let fixture: ComponentFixture<ForgotPasswordComponent>;
  let authServiceMock: any;
  let toastServiceMock: any;

  beforeEach(async () => {
    authServiceMock = {
      forgotPassword: jasmine.createSpy('forgotPassword')
    };

    toastServiceMock = {
      success: jasmine.createSpy('success'),
      error: jasmine.createSpy('error')
    };

    await TestBed.configureTestingModule({
      imports: [
        ForgotPasswordComponent, // ✅ standalone component goes here
        CommonModule,
        FormsModule,
        RouterModule.forRoot([])
      ],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: ToastService, useValue: toastServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ForgotPasswordComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should show error if form is invalid', () => {
    const fakeForm = { invalid: true } as NgForm;
    component.submit(fakeForm);

    expect(toastServiceMock.error).toHaveBeenCalledWith('Please enter a valid email address.');
    expect(authServiceMock.forgotPassword).not.toHaveBeenCalled();
  });

  it('should call authService.forgotPassword and show success on valid form', () => {
    const fakeForm = { invalid: false } as NgForm;
    authServiceMock.forgotPassword.and.returnValue(of({ message: 'Reset link sent.' }));
    component.email = 'test@example.com';

    component.submit(fakeForm);

    expect(authServiceMock.forgotPassword).toHaveBeenCalledWith({ email: 'test@example.com' });
    expect(toastServiceMock.success).toHaveBeenCalledWith('Reset link sent.');
  });

  it('should show error if authService.forgotPassword fails', () => {
    const fakeForm = { invalid: false } as NgForm;
    authServiceMock.forgotPassword.and.returnValue(
      throwError(() => ({ error: { message: 'Server error' } }))
    );
    component.email = 'test@example.com';

    component.submit(fakeForm);

    expect(toastServiceMock.error).toHaveBeenCalledWith('Server error');
  });

  it('should use default error message if error has no message', () => {
    const fakeForm = { invalid: false } as NgForm;
    authServiceMock.forgotPassword.and.returnValue(throwError(() => ({})));
    component.email = 'test@example.com';

    component.submit(fakeForm);

    expect(toastServiceMock.error).toHaveBeenCalledWith('Something went wrong');
  });
});
