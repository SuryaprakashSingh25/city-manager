import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-forgot-password',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent {
  email = '';

  constructor(private auth: AuthService, private toast: ToastService) {}

  submit(form: NgForm) {
    if (form.invalid) {
      this.toast.error('Please enter a valid email address.');
      return;
    }

    this.auth.forgotPassword({ email: this.email }).subscribe({
      next: (res) => this.toast.success(res.message || 'Reset link sent successfully.'),
      error: (err) => this.toast.error(err.error?.message || 'Something went wrong')
    });
  }
}
