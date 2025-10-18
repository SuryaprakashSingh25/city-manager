import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const token = localStorage.getItem('accessToken');

    if (!token || this.isTokenExpired(token)) {
      this.handleInvalidAccess();
      return false;
    }

    const user = this.auth.getUserFromToken();
    if (!user) {
      this.handleInvalidAccess();
      return false;
    }

    const allowedRoles = route.data?.['roles'] as string[] | undefined;
    if (allowedRoles && !allowedRoles.includes(user.role)) {
      this.handleInvalidAccess(user.role);
      return false;
    }

    return true;
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

  private handleInvalidAccess(userRole?: string) {
    // Clear stored tokens
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');

    // Redirect based on role if available
    if (!userRole) {
      this.router.navigate(['/login']);
      return;
    }

    switch (userRole) {
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
