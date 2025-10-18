import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.scss']
})
export class VerifyEmailComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private auth: AuthService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (token) {
      this.auth.verifyEmail(token).subscribe({
        next: () => {
          this.toast.success('Email verified successfully! Redirecting...');
          setTimeout(() => this.router.navigate(['/login']), 2000);
        },
        error: () => {
          this.toast.error('Invalid or expired verification link.');
        }
      });
    } else {
      this.toast.error('Invalid verification request.');
    }
  }
}
