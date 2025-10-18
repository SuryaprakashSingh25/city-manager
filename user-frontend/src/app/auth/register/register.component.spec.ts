import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { NgForm } from '@angular/forms';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceMock: any;
  let toastServiceMock: any;
  let router: Router;

  beforeEach(async () => {
    authServiceMock = {
      register: jasmine.createSpy('register')
    };

    toastServiceMock = {
      success: jasmine.createSpy('success'),
      error: jasmine.createSpy('error')
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: ToastService, useValue: toastServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate'); // spy on navigate
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show error if form is invalid', () => {
    const form = { invalid: true } as NgForm;
    component.register(form);
    expect(toastServiceMock.error).toHaveBeenCalledWith('Please fill out the form correctly.');
  });

  it('should call authService.register and navigate on valid form', () => {
    component.email = 'test@example.com';
    component.password = 'password123';
    const form = { invalid: false } as NgForm;

    authServiceMock.register.and.returnValue(of({ email: 'test@example.com' }));

    component.register(form);

    expect(authServiceMock.register).toHaveBeenCalledWith({
      email: 'test@example.com',
      password: 'password123'
    });

    expect(toastServiceMock.success).toHaveBeenCalledWith(
      'User test@example.com registered successfully. Please check your email to verify your account.'
    );

    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should show default error if registration fails', () => {
    const form = { invalid: false } as NgForm;
    const errorResponse = { error: { message: 'Email already exists' } };
    authServiceMock.register.and.returnValue(throwError(() => errorResponse));

    component.register(form);

    expect(toastServiceMock.error).toHaveBeenCalledWith('Email already exists');
  });

  it('should toggle showPassword', () => {
    expect(component.showPassword).toBeFalse();
    component.togglePassword();
    expect(component.showPassword).toBeTrue();
    component.togglePassword();
    expect(component.showPassword).toBeFalse();
  });
});
