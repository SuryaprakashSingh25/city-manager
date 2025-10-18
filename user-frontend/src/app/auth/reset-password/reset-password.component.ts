import { Component } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-reset-password',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent {
  token: string | null = null;
  newPassword = '';
  confirmPassword = '';
  showNewPassword = false;
  showConfirmPassword = false;

  constructor(
    private route: ActivatedRoute,
    private auth: AuthService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token');
  }

  submit(form: NgForm) {
    if (form.invalid) {
      this.toast.error('Please fill in all required fields correctly.');
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.toast.error('Passwords do not match.');
      return;
    }

    if (this.token) {
      const request = { token: this.token, newPassword: this.newPassword };
      this.auth.resetPassword(request).subscribe({
        next: (res) => {
          this.toast.success(res.message || 'Password reset successfully.');
          setTimeout(() => this.router.navigate(['/login']), 2000);
        },
        error: err => {
          this.toast.error(err.error.message || 'Invalid or expired token');
        }
      });
    }
  }
}
