import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthResponse } from '../../core/models/auth.models';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email = '';
  password = '';
  showPassword = false;

  constructor(
    private auth: AuthService, 
    private router: Router,
    private toast: ToastService
  ) {}

  login(form: NgForm) {
    if (form.invalid) {
      this.toast.error('Please enter valid credentials.');
      return;
    }

    this.auth.login({ email: this.email, password: this.password })
      .subscribe({
        next: (res: AuthResponse) => {
          localStorage.setItem('accessToken', res.accessToken);
          localStorage.setItem('refreshToken', res.refreshToken);

          const payload = JSON.parse(atob(res.accessToken.split('.')[1]));
          const role = payload.role;

          if (role === 'ADMIN') {
            this.router.navigate(['/allUsers']);
          } else if (role === 'CITIZEN') {
            this.router.navigate(['/citizen/issues']);
          } else if (role === 'STAFF') {
            this.router.navigate(['/staff/issues']);
          }
        },
        error: (err) => {
          if (err.status === 401) {
            this.toast.error("Invalid email or password.");
          } else if (err.status === 403) {
            this.toast.error("Please verify your email before logging in.");
          } else {
            this.toast.error("Something went wrong. Please try again later.");
          }
        }
      });
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }
}
