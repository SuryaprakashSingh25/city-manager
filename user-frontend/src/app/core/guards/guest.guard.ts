import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class GuestGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): boolean {
    const token = localStorage.getItem('accessToken');

    if (!token || this.isTokenExpired(token)) {
      // No valid token, allow access to login/register
      return true;
    }

    const user = this.auth.getUserFromToken();
    if (!user) {
      // Invalid token, clear storage and allow access
      this.clearStorageAndRedirectToLogin();
      return true;
    }

    // Logged-in user, redirect based on role
    this.redirectBasedOnRole(user.role);
    return false;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiry = payload.exp * 1000;
      return Date.now() > expiry;
    } catch (err) {
      console.error('Invalid token format', err);
      return true;
    }
  }

  private clearStorageAndRedirectToLogin() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.router.navigate(['/login']);
  }

  private redirectBasedOnRole(role: string) {
    switch (role) {
      case 'ADMIN':
        this.router.navigate(['/allUsers']);
        break;
      case 'CITIZEN':
        this.router.navigate(['/citizen/issues']);
        break;
      case 'STAFF':
        this.router.navigate(['/staff/issues']);
        break;
      default:
        this.router.navigate(['/login']);
    }
  }
}
