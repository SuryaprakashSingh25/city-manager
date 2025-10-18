import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { UserResponse } from '../../core/models/auth.models';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-register',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  email = '';
  password = '';
  showPassword = false;

  constructor(
    private auth: AuthService, 
    private router: Router,
    private toast: ToastService
  ) {}

  register(form: NgForm) {
    if (form.invalid) {
      this.toast.error('Please fill out the form correctly.');
      return;
    }

    this.auth.register({ email: this.email, password: this.password })
      .subscribe({
        next: (res: UserResponse) => {
          this.toast.success(`User ${res.email} registered successfully. Please check your email to verify your account.`);
          this.router.navigate(['/login']);
        },
        error: err => {
          this.toast.error(err.error.message || 'Registration failed');
        }
      });
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }
}
